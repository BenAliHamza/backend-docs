package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.medication.MedicationDto;

import java.util.List;

public interface MedicationService {

    List<MedicationDto> getMedications(String query);

    MedicationDto getMedication(Long id);
}
