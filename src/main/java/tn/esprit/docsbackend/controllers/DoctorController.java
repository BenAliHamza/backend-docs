package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.doctor.DoctorPracticeSetupRequest;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.doctor.DoctorPublicProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorSearchResultDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.services.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // ---------------- current doctor endpoints ----------------

    @GetMapping("/me")
    public DoctorProfileDto getCurrentDoctorProfile() {
        return doctorService.getCurrentDoctorProfile();
    }

    @PutMapping("/me")
    public DoctorProfileDto updateCurrentDoctorProfile(
            @RequestBody DoctorProfileUpdateRequest request
    ) {
        return doctorService.updateCurrentDoctorProfile(request);
    }

    @GetMapping("/me/patients")
    public List<PatientProfileDto> getPatientsOfCurrentDoctor() {
        return doctorService.getPatientsOfCurrentDoctor();
    }

    @PostMapping("/me/patients/{patientUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addPatientToCurrentDoctor(@PathVariable Long patientUserId) {
        doctorService.addPatientToCurrentDoctor(patientUserId);
    }

    @DeleteMapping("/me/patients/{patientUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePatientFromCurrentDoctor(@PathVariable Long patientUserId) {
        doctorService.removePatientFromCurrentDoctor(patientUserId);
    }

    @PostMapping("/me/practice-setup")
    public DoctorProfileDto setupPracticeForCurrentDoctor(
            @RequestBody DoctorPracticeSetupRequest request
    ) {
        return doctorService.setupPracticeForCurrentDoctor(
                request.getSpecialtyId(),
                request.getActIds()
        );
    }

    // ---------------- new public/search endpoints ----------------

    /**
     * Public search endpoint for doctors.
     *
     * Example:
     * GET /api/doctors/search?q=cardio&city=Tunis&teleconsultationEnabled=true
     */
    @GetMapping("/search")
    public List<DoctorSearchResultDto> searchDoctors(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "specialtyId", required = false) Long specialtyId,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "teleconsultationEnabled", required = false) Boolean teleconsultationEnabled,
            @RequestParam(name = "acceptingNewPatients", required = false) Boolean acceptingNewPatients
    ) {
        return doctorService.searchDoctors(
                query,
                specialtyId,
                city,
                country,
                teleconsultationEnabled,
                acceptingNewPatients
        );
    }

    /**
     * Public doctor profile by doctor profile id.
     *
     * GET /api/doctors/{doctorId}/public
     */
    @GetMapping("/{doctorId}/public")
    public DoctorPublicProfileDto getDoctorPublicProfile(@PathVariable Long doctorId) {
        return doctorService.getDoctorPublicProfile(doctorId);
    }
}
