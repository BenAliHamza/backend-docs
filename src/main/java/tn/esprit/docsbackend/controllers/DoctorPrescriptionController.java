package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.dto.medication.PrescriptionCreateRequest;
import tn.esprit.docsbackend.dto.medication.PrescriptionDto;
import tn.esprit.docsbackend.services.PrescriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/doctors/me")
@RequiredArgsConstructor
public class DoctorPrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * POST /api/doctors/me/patients/{patientUserId}/prescriptions
     */
    @PostMapping("/patients/{patientUserId}/prescriptions")
    public ResponseEntity<PrescriptionDto> createPrescriptionForPatient(
            @PathVariable Long patientUserId,
            @RequestBody PrescriptionCreateRequest request
    ) {
        PrescriptionDto dto = prescriptionService
                .createPrescriptionForCurrentDoctorPatient(patientUserId, request);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/doctors/me/patients/{patientUserId}/prescriptions?activeOnly=true
     */
    @GetMapping("/patients/{patientUserId}/prescriptions")
    public ResponseEntity<ListResponse<PrescriptionDto>> getPrescriptionsForPatient(
            @PathVariable Long patientUserId,
            @RequestParam(name = "activeOnly", required = false) Boolean activeOnly
    ) {
        List<PrescriptionDto> list = prescriptionService
                .getPrescriptionsForCurrentDoctorPatient(patientUserId, activeOnly);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * GET /api/doctors/me/prescriptions/{prescriptionId}
     */
    @GetMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<PrescriptionDto> getPrescription(
            @PathVariable Long prescriptionId
    ) {
        PrescriptionDto dto = prescriptionService.getPrescriptionForCurrentDoctor(prescriptionId);
        return ResponseEntity.ok(dto);
    }

    /**
     * DELETE /api/doctors/me/prescriptions/{prescriptionId}
     */
    @DeleteMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<Void> deletePrescription(
            @PathVariable Long prescriptionId
    ) {
        prescriptionService.deletePrescriptionForCurrentDoctor(prescriptionId);
        return ResponseEntity.noContent().build();
    }
}
