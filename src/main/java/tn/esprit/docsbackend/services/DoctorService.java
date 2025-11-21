package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;

import java.util.List;

public interface DoctorService {

    /**
     * Returns the profile of the currently authenticated doctor.
     */
    DoctorProfileDto getCurrentDoctorProfile();

    /**
     * Partially update the profile of the currently authenticated doctor.
     */
    DoctorProfileDto updateCurrentDoctorProfile(DoctorProfileUpdateRequest request);

    /**
     * Returns the list of patients linked to the currently authenticated doctor.
     */
    List<PatientProfileDto> getPatientsOfCurrentDoctor();

    /**
     * Link (affecter) a patient to the currently authenticated doctor.
     * The patient identifier here is the patient's user id.
     */
    void addPatientToCurrentDoctor(Long patientUserId);

    /**
     * Unlink (désaffecter) a patient from the currently authenticated doctor.
     * The patient identifier here is the patient's user id.
     */
    void removePatientFromCurrentDoctor(Long patientUserId);

    /**
     * Onboarding step: choose one specialty and a set of acts for the current doctor.
     */
    DoctorProfileDto setupPracticeForCurrentDoctor(Long specialtyId, List<Long> actIds);
}
