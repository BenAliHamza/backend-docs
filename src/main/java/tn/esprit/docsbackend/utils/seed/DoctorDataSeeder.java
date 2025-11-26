package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.DoctorSchedule;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.DoctorScheduleRepository;
import tn.esprit.docsbackend.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DoctorDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String COUNTRY_TN = "Tunisia";

    // Simple demo arrays – you can tweak as you like
    private static final String[] FIRST_NAMES = {
            "Amine", "Sami", "Youssef", "Aymen", "Mahdi", "Walid", "Nour", "Ons",
            "Ines", "Mariem", "Sarrah", "Khalil", "Oussama", "Rania", "Houssem",
            "Fares", "Bilel", "Seif", "Khaled", "Ahmed"
    };

    private static final String[] LAST_NAMES = {
            "Ben Ali", "Trabelsi", "Bouazizi", "Cherif", "Gharbi", "Saidi",
            "Mansour", "Ben Youssef", "Jlassi", "Ferjani", "Bouhlel", "Lassoued",
            "Nefzi", "Ouali", "Chouchene", "Ben Amor", "Ayari", "Zidi", "Khemiri", "Zaibi"
    };

    private static final String[] TN_CITIES = {
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Sfax", "Sousse",
            "Nabeul", "Bizerte", "Kairouan", "Monastir", "Mahdia", "Gabès",
            "Gafsa", "Kasserine", "Jendouba", "Béja", "Kebili", "Tozeur",
            "Medenine", "Tataouine"
    };

    private final Random random = new Random();

    @Override
    @Transactional
    public void seed() {
        // 1) Ensure DOCTOR_COUNT demo doctors & profiles exist
        for (int i = 1; i <= SeedConstants.DOCTOR_COUNT; i++) {
            final int index = i; // effectively final for lambda
            String email = "doctor" + index + "@example.com";

            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseGet(() -> createDoctorUser(index, email));

            // Ensure a doctor profile exists for this user
            doctorProfileRepository.findByUserIdAndDeletedFalse(user.getId())
                    .orElseGet(() -> {
                        String city = randomCity();
                        DoctorProfile profile = DoctorProfile.builder()
                                .user(user)
                                .bio("Dr. " + user.getFirstname() + " " + user.getLastname()
                                        + " – demo doctor for testing.")
                                .yearsOfExperience(2 + random.nextInt(25)) // 2–26 years
                                .clinicAddress("Clinic " + city + " #" + index)
                                .city(city)
                                .country(COUNTRY_TN)
                                .consultationFee(randomConsultationFee())
                                .acceptsNewPatients(random.nextBoolean())
                                .teleconsultationEnabled(random.nextBoolean())
                                .maxDailyAppointments(10 + random.nextInt(11)) // 10–20
                                .averageConsultationDurationMinutes(15 + random.nextInt(16)) // 15–30
                                .build();
                        DoctorProfile saved = doctorProfileRepository.save(profile);

                        // Create default schedule for this newly created doctor
                        createDefaultScheduleIfMissing(saved);

                        return saved;
                    });
        }

        // 2) Safety net: for any existing doctor without schedule, create default schedule
        //    (useful if some doctors existed from older data before we added schedules)
        List<DoctorProfile> allDoctors = doctorProfileRepository.findAll();
        for (DoctorProfile doctor : allDoctors) {
            if (doctor.isDeleted()) {
                continue;
            }
            createDefaultScheduleIfMissing(doctor);
        }
    }

    private User createDoctorUser(int index, String email) {
        String firstName = randomFirstName();
        String lastName = randomLastName();

        User user = User.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .phone("+21620" + String.format("%07d", index)) // fake TN-like phone
                .password(passwordEncoder.encode(SeedConstants.DEFAULT_PASSWORD))
                .role(Role.DOCTOR)
                .status(UserStatus.ACTIVE)
                .isFirstLogin(false)
                .build();

        return userRepository.save(user);
    }

    private String randomFirstName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    private String randomLastName() {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private String randomCity() {
        return TN_CITIES[random.nextInt(TN_CITIES.length)];
    }

    private BigDecimal randomConsultationFee() {
        // between 60 and 180 in steps of 10
        int value = 60 + random.nextInt(13) * 10;
        return BigDecimal.valueOf(value);
    }

    /**
     * Create the standard weekly schedule (Mon–Fri, 08:00–12:00 & 13:00–17:00)
     * ONLY if the doctor currently has no schedule entries.
     * This makes the seeder idempotent and safe on every restart.
     */
    private void createDefaultScheduleIfMissing(DoctorProfile doctor) {
        if (doctor.getId() == null) {
            return; // not persisted yet
        }

        boolean hasSchedule = doctorScheduleRepository.existsByDoctorIdAndDeletedFalse(doctor.getId());
        if (hasSchedule) {
            // Doctor already has at least one active schedule row: do nothing
            return;
        }

        LocalTime morningStart = LocalTime.of(8, 0);
        LocalTime morningEnd   = LocalTime.of(12, 0);
        LocalTime afternoonStart = LocalTime.of(13, 0);
        LocalTime afternoonEnd   = LocalTime.of(17, 0);

        EnumSet<DayOfWeek> workingDays = EnumSet.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );

        List<DoctorSchedule> entries = new ArrayList<>();

        for (DayOfWeek dow : workingDays) {
            entries.add(
                    DoctorSchedule.builder()
                            .doctor(doctor)
                            .dayOfWeek(dow)
                            .startTime(morningStart)
                            .endTime(morningEnd)
                            .active(true)
                            .build()
            );
            entries.add(
                    DoctorSchedule.builder()
                            .doctor(doctor)
                            .dayOfWeek(dow)
                            .startTime(afternoonStart)
                            .endTime(afternoonEnd)
                            .active(true)
                            .build()
            );
        }

        doctorScheduleRepository.saveAll(entries);
    }
}
