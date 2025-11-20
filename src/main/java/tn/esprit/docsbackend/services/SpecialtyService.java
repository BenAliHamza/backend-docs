package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.doctor.SpecialtyDto;

import java.util.List;

public interface SpecialtyService {

    /**
     * Returns all active specialties.
     * Since specialties are static reference data, this is read-only.
     */
    List<SpecialtyDto> getAllSpecialties();
}
