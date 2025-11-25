package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DoctorDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
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
                        return doctorProfileRepository.save(profile);
                    });
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
}
