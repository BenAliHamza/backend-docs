package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileUpdateRequest;

import java.util.List;

public interface PatientService {

    /**
     * Returns the profile of the currently authenticated patient.
     */
    PatientProfileDto getCurrentPatientProfile();

    /**
     * Partially update the profile of the currently authenticated patient.
     */
    PatientProfileDto updateCurrentPatientProfile(PatientProfileUpdateRequest request);

    /**
     * Returns the list of doctors linked to the currently authenticated patient.
     */
    List<DoctorProfileDto> getDoctorsOfCurrentPatient();
}
