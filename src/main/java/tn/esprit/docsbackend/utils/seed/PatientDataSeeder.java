package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;

import java.time.LocalDate;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class PatientDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String COUNTRY_TN = "Tunisia";

    private static final String[] FIRST_NAMES = {
            "Hatem", "Yassine", "Mohamed", "Hamza", "Ali", "Marouane",
            "Nour", "Sabri", "Imen", "Rahma", "Sana", "Aya",
            "Aicha", "Faten", "Lina", "Mehdi", "Rami", "Ala", "Zied", "Nidhal"
    };

    private static final String[] LAST_NAMES = {
            "Ben Salah", "Ben Ahmed", "Ben Hassine", "Trabelsi", "Saidi",
            "Bouhlel", "Chouikha", "Haddad", "Jebali", "Masmoudi",
            "Essid", "Mlouka", "Mrad", "Kooli", "Jaziri",
            "Bouaziz", "Guesmi", "Sahnoun", "Feki", "Belhaj"
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
        for (int i = 1; i <= SeedConstants.PATIENT_COUNT; i++) {
            final int index = i; // effectively final for lambda
            String email = "patient" + index + "@example.com";

            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseGet(() -> createPatientUser(index, email));

            // Ensure a patient profile exists for this user
            patientProfileRepository.findByUserIdAndDeletedFalse(user.getId())
                    .orElseGet(() -> {
                        String city = randomCity();
                        PatientProfile profile = PatientProfile.builder()
                                .user(user)
                                .gender(random.nextBoolean() ? "MALE" : "FEMALE")
                                .dateOfBirth(randomBirthDate())
                                .city(city)
                                .country(COUNTRY_TN)
                                .address("Rue de " + city + " " + (10 + random.nextInt(90)))
                                .build();
                        return patientProfileRepository.save(profile);
                    });
        }
    }

    private User createPatientUser(int index, String email) {
        String firstName = randomFirstName();
        String lastName = randomLastName();

        User user = User.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .phone("+21621" + String.format("%07d", index))
                .password(passwordEncoder.encode(SeedConstants.DEFAULT_PASSWORD))
                .role(Role.PATIENT)
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

    private LocalDate randomBirthDate() {
        // Between 1950 and 2015, rough demo
        int year = 1950 + random.nextInt(66); // 1950–2015
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28); // keep it simple
        return LocalDate.of(year, month, day);
    }
}
