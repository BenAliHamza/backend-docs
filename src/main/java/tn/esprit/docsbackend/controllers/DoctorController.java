package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
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

    /**
     * GET /api/doctors/me
     * Current doctor's own profile.
     */
    @GetMapping("/me")
    public ResponseEntity<DoctorProfileDto> getMyProfile() {
        return ResponseEntity.ok(doctorService.getCurrentDoctorProfile());
    }

    /**
     * PUT /api/doctors/me
     * Partial update of current doctor's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<DoctorProfileDto> updateMyProfile(
            @RequestBody DoctorProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(doctorService.updateCurrentDoctorProfile(request));
    }

    /**
     * GET /api/doctors/me/patients
     * All patients linked to current doctor.
     */
    @GetMapping("/me/patients")
    public ResponseEntity<ListResponse<PatientProfileDto>> getMyPatients() {
        List<PatientProfileDto> patients = doctorService.getPatientsOfCurrentDoctor();
        return ResponseEntity.ok(ListResponse.of(patients));
    }

    /**
     * GET /api/doctors/me/patients/{patientUserId}
     * Details of ONE patient (by User.id) linked to current doctor.
     */
    @GetMapping("/me/patients/{patientUserId}")
    public ResponseEntity<PatientProfileDto> getMyPatientByUserId(
            @PathVariable Long patientUserId
    ) {
        PatientProfileDto dto = doctorService.getPatientOfCurrentDoctor(patientUserId);
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/doctors/me/patients/{patientUserId}
     * Link a patient (by User.id) to current doctor.
     */
    @PostMapping("/me/patients/{patientUserId}")
    public ResponseEntity<Void> addPatientToMe(
            @PathVariable Long patientUserId
    ) {
        doctorService.addPatientToCurrentDoctor(patientUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/doctors/me/patients/{patientUserId}
     * Remove link between current doctor and a patient.
     */
    @DeleteMapping("/me/patients/{patientUserId}")
    public ResponseEntity<Void> removePatientFromMe(
            @PathVariable Long patientUserId
    ) {
        doctorService.removePatientFromCurrentDoctor(patientUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/doctors/me/practice-setup
     * Simple onboarding: select one specialty + a set of acts.
     */
    @PostMapping("/me/practice-setup")
    public ResponseEntity<DoctorProfileDto> setupPracticeForMe(
            @RequestBody DoctorPracticeSetupRequest request
    ) {
        DoctorProfileDto dto = doctorService.setupPracticeForCurrentDoctor(
                request.getSpecialtyId(),
                request.getActIds()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/doctors/search
     * Public search of doctors.
     */
    @GetMapping("/search")
    public ResponseEntity<ListResponse<DoctorSearchResultDto>> searchDoctors(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "specialtyId", required = false) Long specialtyId,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "teleconsultationEnabled", required = false) Boolean teleconsultationEnabled,
            @RequestParam(name = "acceptingNewPatients", required = false) Boolean acceptingNewPatients
    ) {
        List<DoctorSearchResultDto> results = doctorService.searchDoctors(
                query,
                specialtyId,
                city,
                country,
                teleconsultationEnabled,
                acceptingNewPatients
        );
        return ResponseEntity.ok(ListResponse.of(results));
    }

    /**
     * GET /api/doctors/{doctorId}/public
     * Public profile for a doctor.
     */
    @GetMapping("/{doctorId}/public")
    public ResponseEntity<DoctorPublicProfileDto> getDoctorPublicProfile(
            @PathVariable Long doctorId
    ) {
        DoctorPublicProfileDto dto = doctorService.getDoctorPublicProfile(doctorId);
        return ResponseEntity.ok(dto);
    }
}

