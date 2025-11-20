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

@Component
@RequiredArgsConstructor
public class PatientDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void seed() {
        for (int i = 1; i <= SeedConstants.PATIENT_COUNT; i++) {
            final int index = i; // make it effectively final for lambda
            String email = "patient" + index + "@example.com";

            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseGet(() -> createPatientUser(index, email));

            // Ensure a patient profile exists for this user
            patientProfileRepository.findByUserIdAndDeletedFalse(user.getId())
                    .orElseGet(() -> {
                        PatientProfile profile = PatientProfile.builder()
                                .user(user)
                                // some simple demo defaults
                                .gender("UNKNOWN")
                                .dateOfBirth(LocalDate.of(1990, 1, 1))
                                .build();
                        return patientProfileRepository.save(profile);
                    });
        }
    }

    private User createPatientUser(int index, String email) {
        User user = User.builder()
                .firstname("Patient")
                .lastname(String.valueOf(index))
                .email(email)
                .phone("+100000000" + index)
                .password(passwordEncoder.encode(SeedConstants.DEFAULT_PASSWORD))
                .role(Role.PATIENT)
                .status(UserStatus.ACTIVE)
                .isFirstLogin(false)
                .build();

        return userRepository.save(user);
    }
}
