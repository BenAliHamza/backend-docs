package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.dto.medication.MedicationDto;
import tn.esprit.docsbackend.services.MedicationService;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public ResponseEntity<ListResponse<MedicationDto>> getMedications(
            @RequestParam(name = "q", required = false) String query
    ) {
        List<MedicationDto> list = medicationService.getMedications(query);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationDto> getMedication(@PathVariable Long id) {
        MedicationDto dto = medicationService.getMedication(id);
        return ResponseEntity.ok(dto);
    }
}
