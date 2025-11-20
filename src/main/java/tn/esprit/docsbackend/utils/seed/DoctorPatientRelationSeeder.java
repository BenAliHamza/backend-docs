package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorPatientRelationSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;

    @Override
    @Transactional
    public void seed() {
        log.info("DoctorPatientRelationSeeder: starting to link demo doctors and patients...");

        for (int doctorIndex = 1; doctorIndex <= SeedConstants.DOCTOR_COUNT; doctorIndex++) {
            String doctorEmail = "doctor" + doctorIndex + "@example.com";

            Optional<User> doctorUserOpt = userRepository.findByEmailAndDeletedFalse(doctorEmail);
            if (doctorUserOpt.isEmpty()) {
                log.debug("DoctorPatientRelationSeeder: doctor user {} not found, skipping.", doctorEmail);
                continue;
            }

            User doctorUser = doctorUserOpt.get();
            Optional<DoctorProfile> doctorProfileOpt =
                    doctorProfileRepository.findByUserIdAndDeletedFalse(doctorUser.getId());

            if (doctorProfileOpt.isEmpty()) {
                log.debug("DoctorPatientRelationSeeder: doctor profile for {} not found, skipping.", doctorEmail);
                continue;
            }

            DoctorProfile doctorProfile = doctorProfileOpt.get();

            // For each doctor, assign PATIENTS_PER_DOCTOR patients using a rolling pattern
            for (int offset = 0; offset < SeedConstants.PATIENTS_PER_DOCTOR; offset++) {
                int patientIndex = ((doctorIndex - 1) * SeedConstants.PATIENTS_PER_DOCTOR + offset)
                        % SeedConstants.PATIENT_COUNT + 1;

                String patientEmail = "patient" + patientIndex + "@example.com";

                Optional<User> patientUserOpt = userRepository.findByEmailAndDeletedFalse(patientEmail);
                if (patientUserOpt.isEmpty()) {
                    log.debug("DoctorPatientRelationSeeder: patient user {} not found, skipping.", patientEmail);
                    continue;
                }

                User patientUser = patientUserOpt.get();
                Optional<PatientProfile> patientProfileOpt =
                        patientProfileRepository.findByUserIdAndDeletedFalse(patientUser.getId());

                if (patientProfileOpt.isEmpty()) {
                    log.debug("DoctorPatientRelationSeeder: patient profile for {} not found, skipping.", patientEmail);
                    continue;
                }

                PatientProfile patientProfile = patientProfileOpt.get();

                // Set relationship on owning side (DoctorProfile.patients)
                doctorProfile.getPatients().add(patientProfile);

                // For in-memory consistency (not required for DB persistence)
                patientProfile.getDoctors().add(doctorProfile);
            }

            doctorProfileRepository.save(doctorProfile);
        }

        log.info("DoctorPatientRelationSeeder: linking completed.");
    }
}
