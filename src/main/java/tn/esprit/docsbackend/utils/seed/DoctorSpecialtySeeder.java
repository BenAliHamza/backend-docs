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

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorSpecialtySeeder implements DataSeeder {

    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecialtyRepository specialtyRepository;

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

        for (int i = 0; i < doctors.size(); i++) {
            DoctorProfile doctor = doctors.get(i);

            if (doctor.isDeleted()) {
                continue;
            }

            // If doctor already has a specialty, skip to keep seeding idempotent.
            if (doctor.getSpecialty() != null) {
                continue;
            }

            int index = i % allSpecialties.size();
            Specialty specialty = allSpecialties.get(index);
            doctor.setSpecialty(specialty);
        }

        doctorProfileRepository.saveAll(doctors);
        log.info("DoctorSpecialtySeeder: finished assigning specialties.");
    }
}
