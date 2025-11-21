package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.docsbackend.dto.doctor.ActDto;
import tn.esprit.docsbackend.dto.doctor.SpecialtyDto;
import tn.esprit.docsbackend.services.ActService;
import tn.esprit.docsbackend.services.SpecialtyService;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;
    private final ActService actService;

    @GetMapping
    public List<SpecialtyDto> getAllSpecialties() {
        return specialtyService.getAllSpecialties();
    }

    /**
     * Returns acts for the current doctor filtered by the given specialty.
     * Used in onboarding: after doctor selects specialty, we show available acts.
     */
    @GetMapping("/{specialtyId}/acts")
    public List<ActDto> getActsForSpecialtyForCurrentDoctor(@PathVariable Long specialtyId) {
        return actService.getActsBySpecialtyId(specialtyId);
    }
}
