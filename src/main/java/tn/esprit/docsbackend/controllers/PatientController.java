package tn.esprit.docsbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileUpdateRequest;
import tn.esprit.docsbackend.services.PatientService;

import java.util.List;

/**
 * Endpoints for operations related to patients themselves.
 */
@RestController
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Returns the profile of the currently authenticated patient.
     *
     * GET /patients/me
     */
    @GetMapping("/patients/me")
    public ResponseEntity<PatientProfileDto> getMyProfile() {
        PatientProfileDto dto = patientService.getCurrentPatientProfile();
        return ResponseEntity.ok(dto);
    }

    /**
     * Partially update the profile of the currently authenticated patient.
     *
     * PUT /patients/me
     */
    @PutMapping("/patients/me")
    public ResponseEntity<PatientProfileDto> updateMyProfile(
            @Valid @RequestBody PatientProfileUpdateRequest request
    ) {
        PatientProfileDto updated = patientService.updateCurrentPatientProfile(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Returns the list of doctors linked to the currently authenticated patient.
     *
     * GET /patients/me/doctors
     */
    @GetMapping("/patients/me/doctors")
    public ResponseEntity<List<DoctorProfileDto>> getMyDoctors() {
        List<DoctorProfileDto> doctors = patientService.getDoctorsOfCurrentPatient();
        return ResponseEntity.ok(doctors);
    }
}
