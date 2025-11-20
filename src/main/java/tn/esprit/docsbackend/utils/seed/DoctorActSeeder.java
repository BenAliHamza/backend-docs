package tn.esprit.docsbackend.utils.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.Act;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.repositories.ActRepository;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorActSeeder implements DataSeeder {

    private final ActRepository actRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SeedJsonLoader seedJsonLoader;

    /**
     * DTO used only for JSON seeding of global act templates.
     */
    public static class ActTemplate {
        private String code;
        private String name;
        private String description;
        private BigDecimal basePrice;
        private Integer defaultDurationMinutes;
        private Boolean teleconsultationAvailable;
        private Set<String> specialties;

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

        public BigDecimal getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
        }

        public Integer getDefaultDurationMinutes() {
            return defaultDurationMinutes;
        }

        public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
            this.defaultDurationMinutes = defaultDurationMinutes;
        }

        public Boolean getTeleconsultationAvailable() {
            return teleconsultationAvailable;
        }

        public void setTeleconsultationAvailable(Boolean teleconsultationAvailable) {
            this.teleconsultationAvailable = teleconsultationAvailable;
        }

        public Set<String> getSpecialties() {
            return specialties;
        }

        public void setSpecialties(Set<String> specialties) {
            this.specialties = specialties;
        }
    }

    @Override
    @Transactional
    public void seed() {
        List<ActTemplate> templates = seedJsonLoader.loadList(
                "seed/acts.json",
                new TypeReference<List<ActTemplate>>() {}
        );

        if (templates.isEmpty()) {
            log.warn("DoctorActSeeder: no act templates loaded from JSON; skipping.");
            return;
        }

        List<DoctorProfile> doctors = doctorProfileRepository.findAll();
        log.info("DoctorActSeeder: checking acts for {} doctors...", doctors.size());

        for (DoctorProfile doctor : doctors) {
            if (doctor.isDeleted()) {
                continue;
            }

            // If this doctor already has acts, skip (idempotent).
            boolean hasActs = !actRepository.findByDoctorIdAndDeletedFalse(doctor.getId()).isEmpty();
            if (hasActs) {
                continue;
            }

            Set<Specialty> doctorSpecialties = doctor.getSpecialties();
            if (doctorSpecialties == null || doctorSpecialties.isEmpty()) {
                continue;
            }

            // Normalize doctor specialty codes for fast lookup
            Set<String> doctorSpecialtyCodes = new HashSet<>();
            for (Specialty specialty : doctorSpecialties) {
                if (specialty != null && specialty.getCode() != null) {
                    doctorSpecialtyCodes.add(specialty.getCode().toUpperCase());
                }
            }

            if (doctorSpecialtyCodes.isEmpty()) {
                continue;
            }

            List<Act> actsToCreate = new ArrayList<>();
            for (ActTemplate template : templates) {
                if (template == null || template.getSpecialties() == null || template.getSpecialties().isEmpty()) {
                    continue;
                }

                // Check if this template applies to at least one of the doctor's specialties
                boolean applicable = template.getSpecialties().stream()
                        .filter(Objects::nonNull)
                        .map(String::toUpperCase)
                        .anyMatch(doctorSpecialtyCodes::contains);

                if (!applicable) {
                    continue;
                }

                Act act = Act.builder()
                        .doctor(doctor)
                        .code(template.getCode())
                        .name(template.getName())
                        .description(template.getDescription())
                        .basePrice(template.getBasePrice())
                        .defaultDurationMinutes(template.getDefaultDurationMinutes())
                        .teleconsultationAvailable(template.getTeleconsultationAvailable())
                        .build();

                actsToCreate.add(act);
            }

            if (!actsToCreate.isEmpty()) {
                actRepository.saveAll(actsToCreate);
                log.info("DoctorActSeeder: created {} acts for doctor profile id={}", actsToCreate.size(), doctor.getId());
            }
        }
    }
}
