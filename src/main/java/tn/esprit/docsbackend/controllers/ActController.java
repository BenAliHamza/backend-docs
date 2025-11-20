package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.docsbackend.dto.doctor.ActDto;
import tn.esprit.docsbackend.services.ActService;

import java.util.List;

/**
 * Endpoints related to acts/services.
 */
@RestController
@RequiredArgsConstructor
public class ActController {

    private final ActService actService;

    /**
     * Returns the acts/services available for the currently authenticated doctor.
     *
     * GET /doctors/me/acts
     */
    @GetMapping("/doctors/me/acts")
    public ResponseEntity<List<ActDto>> getMyActs() {
        List<ActDto> acts = actService.getActsForCurrentDoctor();
        return ResponseEntity.ok(acts);
    }
}
