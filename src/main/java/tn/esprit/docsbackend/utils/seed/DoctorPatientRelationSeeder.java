package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.entities.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorPatientRelationSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void seed() {
        log.info("DoctorPatientRelationSeeder: starting to link demo doctors and patients...");

        // Get all non-deleted patient profiles
        List<PatientProfile> allPatients = patientProfileRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toList());

        if (allPatients.isEmpty()) {
            log.warn("DoctorPatientRelationSeeder: no patient profiles found, skipping.");
            return;
        }

        List<DoctorProfile> doctors = doctorProfileRepository.findAll();
        if (doctors.isEmpty()) {
            log.warn("DoctorPatientRelationSeeder: no doctor profiles found, skipping.");
            return;
        }

        for (DoctorProfile doctorProfile : doctors) {
            if (doctorProfile.isDeleted()) {
                continue;
            }

            User doctorUser = doctorProfile.getUser();
            String doctorEmail = doctorUser != null ? doctorUser.getEmail() : "unknown";
            log.debug("DoctorPatientRelationSeeder: processing doctor {}", doctorEmail);

            Set<PatientProfile> currentPatients =
                    doctorProfile.getPatients() != null
                            ? new HashSet<>(doctorProfile.getPatients())
                            : new HashSet<>();

            // If doctor already has enough patients, keep it as is (idempotent behavior)
            if (currentPatients.size() >= SeedConstants.PATIENTS_PER_DOCTOR) {
                log.debug("DoctorPatientRelationSeeder: doctor {} already has {} patients, skipping.",
                        doctorEmail, currentPatients.size());
                continue;
            }

            int targetCount = SeedConstants.PATIENTS_PER_DOCTOR;
            int attempts = 0;
            int maxAttempts = allPatients.size() * 2;

            while (currentPatients.size() < targetCount && attempts < maxAttempts) {
                PatientProfile randomPatient = allPatients.get(random.nextInt(allPatients.size()));
                currentPatients.add(randomPatient);
                // maintain bidirectional relationship
                randomPatient.getDoctors().add(doctorProfile);
                attempts++;
            }

            doctorProfile.setPatients(currentPatients);
            doctorProfileRepository.save(doctorProfile);

            log.debug("DoctorPatientRelationSeeder: doctor {} now linked to {} patients.",
                    doctorEmail, currentPatients.size());
        }

        log.info("DoctorPatientRelationSeeder: linking completed.");
    }
}
