package tn.esprit.docsbackend.utils.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
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
     */
    public static class SpecialtySeed {
        private String code;
        private String name;
        private String description;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
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

            boolean exists = specialtyRepository.existsByNameIgnoreCaseAndDeletedFalse(seed.getName());
            if (exists) {
                continue;
            }

            Specialty specialty = Specialty.builder()
                    .code(seed.getCode())
                    .name(seed.getName())
                    .description(seed.getDescription())
                    .active(true)
                    .build();

            specialtyRepository.save(specialty);
            log.info("SpecialtyDataSeeder: created specialty {} ({})", seed.getName(), seed.getCode());
        }
    }
}
