package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.medication.MedicationDto;
import tn.esprit.docsbackend.entities.Medication;
import tn.esprit.docsbackend.repositories.MedicationRepository;
import tn.esprit.docsbackend.services.MedicationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDto> getMedications(String query) {
        List<Medication> meds;
        if (query == null || query.isBlank()) {
            meds = medicationRepository.findByDeletedFalseAndActiveTrueOrderByNameAsc();
        } else {
            meds = medicationRepository
                    .findByDeletedFalseAndActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        }

        return meds.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationDto getMedication(Long id) {
        Medication med = medicationRepository.findById(id)
                .filter(m -> !m.isDeleted() && Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Medication with id=" + id + " not found"
                ));

        return toDto(med);
    }

    private MedicationDto toDto(Medication med) {
        return MedicationDto.builder()
                .id(med.getId())
                .code(med.getCode())
                .name(med.getName())
                .description(med.getDescription())
                .active(med.getActive())
                .build();
    }
}
