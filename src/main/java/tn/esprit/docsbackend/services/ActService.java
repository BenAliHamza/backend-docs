package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.doctor.ActDto;

import java.util.List;

public interface ActService {

    /**
     * Returns the list of acts/services for the currently authenticated doctor.
     */
    List<ActDto> getActsForCurrentDoctor();
}
