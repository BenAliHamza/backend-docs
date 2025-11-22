package tn.esprit.docsbackend.utils.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.repositories.SpecialtyRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpecialtyDataSeeder implements DataSeeder {

    private final SpecialtyRepository specialtyRepository;
    private final SeedJsonLoader seedJsonLoader;

    /**
     * DTO used only for JSON seeding.
     * The "id" field comes from JSON but we DO NOT map it to the entity id,
     * because the entity uses IDENTITY auto-generation.
     */
    @Setter
    @Getter
    public static class SpecialtySeed {
        private Long id;          // informational only, DO NOT push to entity id
        private String code;
        private String name;
        private String description;
    }

    @Override
    @Transactional
    public void seed() {
        long existingCount = specialtyRepository.count();
        log.info("SpecialtyDataSeeder: existing specialties count = {}", existingCount);

        List<SpecialtySeed> seeds = seedJsonLoader.loadList(
                "seed/specialties.json",
                new TypeReference<List<SpecialtySeed>>() {}
        );

        if (seeds.isEmpty()) {
            log.warn("SpecialtyDataSeeder: no specialties loaded from JSON; skipping.");
            return;
        }

        for (SpecialtySeed seed : seeds) {
            if (seed == null || seed.getName() == null) {
                continue;
            }

            // Idempotency: skip if a non-deleted specialty with same name already exists
            boolean exists = specialtyRepository.existsByNameIgnoreCaseAndDeletedFalse(seed.getName());
            if (exists) {
                log.debug("SpecialtyDataSeeder: specialty '{}' already exists, skipping.", seed.getName());
                continue;
            }

            Specialty specialty = Specialty.builder()
                    .code(seed.getCode())
                    .name(seed.getName())
                    .description(seed.getDescription())
                    .active(true)
                    .build();

            // ⚠️ IMPORTANT:
            // Do NOT set specialty.setId(seed.getId());
            // The entity id is auto-generated (IDENTITY), and forcing it
            // breaks Hibernate's insert/update detection and causes
            // StaleObjectStateException / ObjectOptimisticLockingFailureException.

            Specialty saved = specialtyRepository.save(specialty);
            log.info(
                    "SpecialtyDataSeeder: created specialty '{}' (code={}, jsonId={}, dbId={})",
                    seed.getName(),
                    seed.getCode(),
                    seed.getId(),
                    saved.getId()
            );
        }
    }
}
