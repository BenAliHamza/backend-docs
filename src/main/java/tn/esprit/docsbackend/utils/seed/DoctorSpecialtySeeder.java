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

    private static final int MIN_SPECIALTIES_PER_DOCTOR = 1;
    private static final int MAX_SPECIALTIES_PER_DOCTOR = 2;

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

            // If doctor already has specialties, skip to keep seeding idempotent.
            if (doctor.getSpecialties() != null && !doctor.getSpecialties().isEmpty()) {
                continue;
            }

            int specialtiesToAssign = (i % 2 == 0) ? MAX_SPECIALTIES_PER_DOCTOR : MIN_SPECIALTIES_PER_DOCTOR;

            if (doctor.getSpecialties() == null) {
                doctor.setSpecialties(new java.util.HashSet<>());
            }

            int index = i % allSpecialties.size();
            Specialty first = allSpecialties.get(index);
            doctor.getSpecialties().add(first);

            if (specialtiesToAssign > 1 && allSpecialties.size() > 1) {
                int index2 = (i + 3) % allSpecialties.size();
                Specialty second = allSpecialties.get(index2);
                doctor.getSpecialties().add(second);
            }
        }

        doctorProfileRepository.saveAll(doctors);
        log.info("DoctorSpecialtySeeder: finished assigning specialties.");
    }
}
