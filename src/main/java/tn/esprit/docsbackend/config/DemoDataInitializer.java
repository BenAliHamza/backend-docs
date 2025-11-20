package tn.esprit.docsbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.utils.seed.DoctorActSeeder;
import tn.esprit.docsbackend.utils.seed.DoctorDataSeeder;
import tn.esprit.docsbackend.utils.seed.DoctorPatientRelationSeeder;
import tn.esprit.docsbackend.utils.seed.DoctorSpecialtySeeder;
import tn.esprit.docsbackend.utils.seed.PatientDataSeeder;
import tn.esprit.docsbackend.utils.seed.SpecialtyDataSeeder;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientDataSeeder patientDataSeeder;
    private final DoctorDataSeeder doctorDataSeeder;
    private final DoctorPatientRelationSeeder doctorPatientRelationSeeder;
    private final SpecialtyDataSeeder specialtyDataSeeder;
    private final DoctorSpecialtySeeder doctorSpecialtySeeder;
    private final DoctorActSeeder doctorActSeeder;

    @Override
    public void run(String... args) {
        long existingUsers = userRepository.count();
        log.info("DemoDataInitializer: existing users count = {}", existingUsers);

        // 1) Static reference data: specialties
        log.info("DemoDataInitializer: seeding specialties...");
        specialtyDataSeeder.seed();

        // 2) Demo users + profiles
        log.info("DemoDataInitializer: seeding demo patients and doctors (existing users will be preserved)...");
        patientDataSeeder.seed();
        doctorDataSeeder.seed();

        // 3) Demo relationships patient <-> doctor
        log.info("DemoDataInitializer: seeding doctor-patient relationships...");
        doctorPatientRelationSeeder.seed();

        // 4) Doctor specialties and acts (based on specialties)
        log.info("DemoDataInitializer: seeding doctor specialties...");
        doctorSpecialtySeeder.seed();

        log.info("DemoDataInitializer: seeding doctor acts based on specialties...");
        doctorActSeeder.seed();

        log.info("DemoDataInitializer: demo data seeding finished.");
    }
}
