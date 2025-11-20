package tn.esprit.docsbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.services.DoctorService;

import java.util.List;

/**
 * Endpoints for operations related to doctors themselves.
 */
@RestController
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Returns the profile of the currently authenticated doctor.
     *
     * GET /doctors/me
     */
    @GetMapping("/doctors/me")
    public ResponseEntity<DoctorProfileDto> getMyProfile() {
        DoctorProfileDto dto = doctorService.getCurrentDoctorProfile();
        return ResponseEntity.ok(dto);
    }

    /**
     * Partially update the profile of the currently authenticated doctor.
     *
     * PUT /doctors/me
     */
    @PutMapping("/doctors/me")
    public ResponseEntity<DoctorProfileDto> updateMyProfile(
            @Valid @RequestBody DoctorProfileUpdateRequest request
    ) {
        DoctorProfileDto updated = doctorService.updateCurrentDoctorProfile(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Returns the list of patients linked to the currently authenticated doctor.
     *
     * GET /doctors/me/patients
     */
    @GetMapping("/doctors/me/patients")
    public ResponseEntity<List<PatientProfileDto>> getMyPatients() {
        List<PatientProfileDto> patients = doctorService.getPatientsOfCurrentDoctor();
        return ResponseEntity.ok(patients);
    }

    /**
     * Link (affecter) a patient to the currently authenticated doctor.
     * The path variable is the patient's user id.
     *
     * POST /doctors/me/patients/{patientUserId}
     */
    @PostMapping("/doctors/me/patients/{patientUserId}")
    public ResponseEntity<Void> addPatientToMe(@PathVariable Long patientUserId) {
        doctorService.addPatientToCurrentDoctor(patientUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unlink (désaffecter) a patient from the currently authenticated doctor.
     * The path variable is the patient's user id.
     *
     * DELETE /doctors/me/patients/{patientUserId}
     */
    @DeleteMapping("/doctors/me/patients/{patientUserId}")
    public ResponseEntity<Void> removePatientFromMe(@PathVariable Long patientUserId) {
        doctorService.removePatientFromCurrentDoctor(patientUserId);
        return ResponseEntity.noContent().build();
    }
}
