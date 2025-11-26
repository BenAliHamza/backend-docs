package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.medication.PrescriptionCreateRequest;
import tn.esprit.docsbackend.dto.medication.PrescriptionDto;
import tn.esprit.docsbackend.dto.medication.PrescriptionLineDto;

import java.util.List;

public interface PrescriptionService {

    // Doctor side
    PrescriptionDto createPrescriptionForCurrentDoctorPatient(Long patientUserId, PrescriptionCreateRequest request);

    List<PrescriptionDto> getPrescriptionsForCurrentDoctorPatient(Long patientUserId, Boolean activeOnly);

    PrescriptionDto getPrescriptionForCurrentDoctor(Long prescriptionId);

    void deletePrescriptionForCurrentDoctor(Long prescriptionId);

    // Patient side
    List<PrescriptionDto> getPrescriptionsForCurrentPatient(Boolean activeOnly);

    PrescriptionDto getPrescriptionForCurrentPatient(Long prescriptionId);

    List<PrescriptionLineDto> getActiveLinesForCurrentPatient();

    PrescriptionLineDto updateReminderForCurrentPatientLine(Long lineId, Boolean reminderEnabled);
}
