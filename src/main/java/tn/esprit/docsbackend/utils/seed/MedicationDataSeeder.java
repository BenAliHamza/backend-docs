package tn.esprit.docsbackend.utils.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.Medication;
import tn.esprit.docsbackend.repositories.MedicationRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicationDataSeeder implements DataSeeder {

    private final MedicationRepository medicationRepository;
    private final SeedJsonLoader seedJsonLoader;

    @Getter
    @Setter
    public static class MedicationSeed {
        private String code;
        private String name;
        private String description;
    }

    @Override
    @Transactional
    public void seed() {
        long existingCount = medicationRepository.count();
        log.info("MedicationDataSeeder: existing medications count = {}", existingCount);

        List<MedicationSeed> seeds = seedJsonLoader.loadList(
                "seed/medications.json",
                new TypeReference<List<MedicationSeed>>() {}
        );

        if (seeds.isEmpty()) {
            log.warn("MedicationDataSeeder: no medications loaded from JSON; skipping.");
            return;
        }

        for (MedicationSeed seed : seeds) {
            if (seed == null || seed.getName() == null) {
                continue;
            }

            boolean exists = medicationRepository.existsByNameIgnoreCaseAndDeletedFalse(seed.getName());
            if (exists) {
                log.debug("MedicationDataSeeder: medication '{}' already exists, skipping.", seed.getName());
                continue;
            }

            Medication med = Medication.builder()
                    .code(seed.getCode())
                    .name(seed.getName())
                    .description(seed.getDescription())
                    .active(true)
                    .build();

            medicationRepository.save(med);
        }

        log.info("MedicationDataSeeder: seeding completed.");
    }
}
