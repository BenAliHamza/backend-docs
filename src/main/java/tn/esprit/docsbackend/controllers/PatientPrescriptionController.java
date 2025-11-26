package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.dto.medication.PrescriptionDto;
import tn.esprit.docsbackend.dto.medication.PrescriptionLineDto;
import tn.esprit.docsbackend.dto.medication.PrescriptionLineReminderUpdateRequest;
import tn.esprit.docsbackend.services.PrescriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions/me")
@RequiredArgsConstructor
public class PatientPrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * GET /api/prescriptions/me?activeOnly=true
     */
    @GetMapping
    public ResponseEntity<ListResponse<PrescriptionDto>> getMyPrescriptions(
            @RequestParam(name = "activeOnly", required = false) Boolean activeOnly
    ) {
        List<PrescriptionDto> list = prescriptionService
                .getPrescriptionsForCurrentPatient(activeOnly);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * GET /api/prescriptions/me/{prescriptionId}
     */
    @GetMapping("/{prescriptionId}")
    public ResponseEntity<PrescriptionDto> getMyPrescription(
            @PathVariable Long prescriptionId
    ) {
        PrescriptionDto dto = prescriptionService.getPrescriptionForCurrentPatient(prescriptionId);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/prescriptions/me/lines/active
     */
    @GetMapping("/lines/active")
    public ResponseEntity<ListResponse<PrescriptionLineDto>> getMyActiveLines() {
        List<PrescriptionLineDto> list = prescriptionService.getActiveLinesForCurrentPatient();
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * PATCH /api/prescriptions/me/lines/{lineId}/reminder
     */
    @PatchMapping("/lines/{lineId}/reminder")
    public ResponseEntity<PrescriptionLineDto> updateLineReminder(
            @PathVariable Long lineId,
            @RequestBody PrescriptionLineReminderUpdateRequest request
    ) {
        PrescriptionLineDto dto = prescriptionService
                .updateReminderForCurrentPatientLine(lineId, request.getReminderEnabled());
        return ResponseEntity.ok(dto);
    }
}
