package tn.esprit.docsbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.doctor.DoctorPracticeSetupRequest;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.services.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/me")
    public DoctorProfileDto getCurrentDoctorProfile() {
        return doctorService.getCurrentDoctorProfile();
    }

    @PutMapping("/me")
    public DoctorProfileDto updateCurrentDoctorProfile(@Valid @RequestBody DoctorProfileUpdateRequest request) {
        return doctorService.updateCurrentDoctorProfile(request);
    }

    @GetMapping("/me/patients")
    public List<PatientProfileDto> getPatientsOfCurrentDoctor() {
        return doctorService.getPatientsOfCurrentDoctor();
    }

    @PostMapping("/me/patients/{patientUserId}")
    public void addPatientToCurrentDoctor(@PathVariable Long patientUserId) {
        doctorService.addPatientToCurrentDoctor(patientUserId);
    }

    @DeleteMapping("/me/patients/{patientUserId}")
    public void removePatientFromCurrentDoctor(@PathVariable Long patientUserId) {
        doctorService.removePatientFromCurrentDoctor(patientUserId);
    }

    /**
     * Onboarding step: doctor chooses one specialty and the acts they perform.
     */
    @PutMapping("/me/practice-setup")
    public DoctorProfileDto setupPracticeForCurrentDoctor(
            @Valid @RequestBody DoctorPracticeSetupRequest request) {

        return doctorService.setupPracticeForCurrentDoctor(
                request.getSpecialtyId(),
                request.getActIds()
        );
    }
}
