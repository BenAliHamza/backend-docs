package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.doctor.*;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;

import java.util.List;

public interface DoctorService {

    /**
     * Get profile of the currently authenticated doctor.
     */
    DoctorProfileDto getCurrentDoctorProfile();

    /**
     * Update profile for the currently authenticated doctor (partial update).
     */
    DoctorProfileDto updateCurrentDoctorProfile(DoctorProfileUpdateRequest request);

    /**
     * Get patients linked to the currently authenticated doctor.
     */
    List<PatientProfileDto> getPatientsOfCurrentDoctor();

    /**
     * Get a single patient (by their User.id) linked to the currently authenticated doctor.
     */
    PatientProfileDto getPatientOfCurrentDoctor(Long patientUserId);

    /**
     * Link a patient (by their user id) to the currently authenticated doctor.
     */
    void addPatientToCurrentDoctor(Long patientUserId);

    /**
     * Remove the link between the currently authenticated doctor and a patient (by patient user id).
     */
    void removePatientFromCurrentDoctor(Long patientUserId);

    /**
     * Setup practice for the current doctor (specialty + acts) during onboarding.
     */
    DoctorProfileDto setupPracticeForCurrentDoctor(Long specialtyId, List<Long> actIds);

    /**
     * Search doctors by optional filters.
     *
     * @param query                  free text on doctor name / specialty
     * @param specialtyId            filter by specialty
     * @param city                   filter by city (exact match, case-insensitive)
     * @param country                filter by country (exact match, case-insensitive)
     * @param teleconsultationEnabled filter by teleconsultation flag
     * @param acceptingNewPatients    filter by accepting new patients flag
     */
    List<DoctorSearchResultDto> searchDoctors(
            String query,
            Long specialtyId,
            String city,
            String country,
            Boolean teleconsultationEnabled,
            Boolean acceptingNewPatients
    );

    /**
     * Public profile for any doctor by doctor profile id.
     */
    DoctorPublicProfileDto getDoctorPublicProfile(Long doctorId);

    DoctorHomeStatsDto getHomeStatsForCurrentDoctor();

}
