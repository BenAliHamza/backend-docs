package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.SpecialtyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorSpecialtySeeder implements DataSeeder {

    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecialtyRepository specialtyRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public void seed() {
        List<Specialty> allSpecialties = new ArrayList<>(specialtyRepository.findAll());
        if (allSpecialties.isEmpty()) {
            log.warn("DoctorSpecialtySeeder: no specialties found; skipping doctor-specialty seeding.");
            return;
        }

        List<DoctorProfile> doctors = doctorProfileRepository.findAll();
        log.info("DoctorSpecialtySeeder: assigning specialties to {} doctors...", doctors.size());

        for (DoctorProfile doctor : doctors) {
            if (doctor.isDeleted()) {
                continue;
            }

            // Idempotency: if doctor already has a specialty, skip
            if (doctor.getSpecialty() != null) {
                continue;
            }

            Specialty randomSpecialty = allSpecialties.get(random.nextInt(allSpecialties.size()));
            doctor.setSpecialty(randomSpecialty);
        }

        doctorProfileRepository.saveAll(doctors);
        log.info("DoctorSpecialtySeeder: finished assigning specialties.");
    }
}
