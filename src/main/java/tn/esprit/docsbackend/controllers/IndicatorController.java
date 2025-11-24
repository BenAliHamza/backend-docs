package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.dto.indicator.IndicatorTypeDto;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorCreateRequest;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorDto;
import tn.esprit.docsbackend.services.PatientIndicatorService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class IndicatorController {

    private final PatientIndicatorService patientIndicatorService;

    /**
     * Static catalog of available indicator types.
     * GET /indicator-types
     */
    @GetMapping("/indicator-types")
    public ResponseEntity<ListResponse<IndicatorTypeDto>> getIndicatorTypes() {
        List<IndicatorTypeDto> types = patientIndicatorService.getAllIndicatorTypes();
        return ResponseEntity.ok(ListResponse.of(types));
    }

    /**
     * Current patient adds a new indicator measurement.
     * POST /indicators/me
     */
    @PostMapping("/indicators/me")
    public ResponseEntity<PatientIndicatorDto> addIndicatorForMe(
            @RequestBody PatientIndicatorCreateRequest request
    ) {
        PatientIndicatorDto dto = patientIndicatorService.addIndicatorForCurrentPatient(request);
        return ResponseEntity.ok(dto);
    }

    /**
     * Current patient lists their own indicators.
     * Optional query params: indicatorTypeId, from, to
     * GET /indicators/me
     */
    @GetMapping("/indicators/me")
    public ResponseEntity<ListResponse<PatientIndicatorDto>> getIndicatorsForMe(
            @RequestParam(name = "indicatorTypeId", required = false) Long indicatorTypeId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<PatientIndicatorDto> list =
                patientIndicatorService.getIndicatorsForCurrentPatient(indicatorTypeId, from, to);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * Delete one of *my* indicators (soft delete).
     *
     * DELETE /indicators/me/{id}
     *
     * - Only works for the currently authenticated PATIENT.
     * - Should return 204 NO_CONTENT on success.
     * - Service is responsible for:
     *      - checking ownership
     *      - checking not already deleted
     *      - throwing 404 / 403 when appropriate.
     */
    @DeleteMapping("/indicators/me/{id}")
    public ResponseEntity<Void> deleteIndicatorForMe(@PathVariable("id") Long id) {
        patientIndicatorService.deleteIndicatorForCurrentPatient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Doctor lists indicators of a given patient (by patient User.id).
     * Requires current user to be DOCTOR and linked to that patient.
     *
     * GET /indicators/patient/{patientUserId}
     */
    @GetMapping("/indicators/patient/{patientUserId}")
    public ResponseEntity<ListResponse<PatientIndicatorDto>> getIndicatorsForPatientAsDoctor(
            @PathVariable Long patientUserId,
            @RequestParam(name = "indicatorTypeId", required = false) Long indicatorTypeId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<PatientIndicatorDto> list =
                patientIndicatorService.getIndicatorsForPatientAsDoctor(patientUserId, indicatorTypeId, from, to);
        return ResponseEntity.ok(ListResponse.of(list));
    }
}
