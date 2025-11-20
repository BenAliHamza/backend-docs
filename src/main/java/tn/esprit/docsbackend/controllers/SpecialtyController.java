package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.docsbackend.dto.doctor.SpecialtyDto;
import tn.esprit.docsbackend.services.SpecialtyService;

import java.util.List;

/**
 * Read-only endpoints for specialties catalog.
 */
@RestController
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    /**
     * Returns the full list of specialties.
     *
     * GET /specialties
     */
    @GetMapping("/specialties")
    public ResponseEntity<List<SpecialtyDto>> getAllSpecialties() {
        List<SpecialtyDto> specialties = specialtyService.getAllSpecialties();
        return ResponseEntity.ok(specialties);
    }
}
