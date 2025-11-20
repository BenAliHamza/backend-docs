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

@Component
@RequiredArgsConstructor
public class DoctorDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void seed() {
        for (int i = 1; i <= SeedConstants.DOCTOR_COUNT; i++) {
            final int index = i; // make it effectively final for lambda
            String email = "doctor" + index + "@example.com";

            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseGet(() -> createDoctorUser(index, email));

            // Ensure a doctor profile exists for this user
            doctorProfileRepository.findByUserIdAndDeletedFalse(user.getId())
                    .orElseGet(() -> {
                        DoctorProfile profile = DoctorProfile.builder()
                                .user(user)
                                .bio("Demo doctor #" + index)
                                .yearsOfExperience(5 + index)
                                .clinicAddress("Demo Clinic " + index)
                                .consultationFee(BigDecimal.valueOf(50 + index * 5L))
                                .build();
                        return doctorProfileRepository.save(profile);
                    });
        }
    }

    private User createDoctorUser(int index, String email) {
        User user = User.builder()
                .firstname("Doctor")
                .lastname(String.valueOf(index))
                .email(email)
                .phone("+200000000" + index)
                .password(passwordEncoder.encode(SeedConstants.DEFAULT_PASSWORD))
                .role(Role.DOCTOR)
                .status(UserStatus.ACTIVE)
                .isFirstLogin(false)
                .build();

        return userRepository.save(user);
    }
}
