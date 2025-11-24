package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.indicator.IndicatorTypeDto;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorCreateRequest;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientIndicatorService {

    /**
     * All active indicator types (static catalog).
     */
    List<IndicatorTypeDto> getAllIndicatorTypes();

    /**
     * Current patient records a new indicator measurement.
     */
    PatientIndicatorDto addIndicatorForCurrentPatient(PatientIndicatorCreateRequest request);

    /**
     * List measurements for current patient (for their own view).
     */
    List<PatientIndicatorDto> getIndicatorsForCurrentPatient(
            Long indicatorTypeId,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * As a doctor, view indicators of a given patient.
     * patientUserId = User.id of the patient.
     */
    List<PatientIndicatorDto> getIndicatorsForPatientAsDoctor(
            Long patientUserId,
            Long indicatorTypeId,
            LocalDateTime from,
            LocalDateTime to
    );

    void deleteIndicatorForCurrentPatient(Long id);
}
