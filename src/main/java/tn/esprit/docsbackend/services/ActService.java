package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.doctor.ActDto;

import java.util.List;

public interface ActService {

    /**
     * Returns the list of acts/services for the currently authenticated doctor.
     */
    List<ActDto> getActsForCurrentDoctor();

    /**
     * Returns the list of acts for the currently authenticated doctor,
     * filtered by the given specialty id.
     */
    List<ActDto> getActsBySpecialtyId(Long specialtyId);
}
