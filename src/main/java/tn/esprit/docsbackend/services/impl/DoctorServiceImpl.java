package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.mappers.DoctorProfileMapper;
import tn.esprit.docsbackend.mappers.PatientProfileMapper;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.services.DoctorService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PatientProfileMapper patientProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileDto getCurrentDoctorProfile() {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        if (currentUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can access this resource");
        }

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        return doctorProfileMapper.toDto(profile);
    }

    /**
     * Update the profile of the currently authenticated doctor (partial update).
     */
    @Override
    @Transactional
    public DoctorProfileDto updateCurrentDoctorProfile(DoctorProfileUpdateRequest request) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        // Delegate field-level update logic to the mapper for consistency.
        doctorProfileMapper.updateEntityFromRequest(request, profile);

        DoctorProfile saved = doctorProfileRepository.save(profile);
        return doctorProfileMapper.toDto(saved);
    }

    /**
     * Returns the list of patients linked to the currently authenticated doctor.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileDto> getPatientsOfCurrentDoctor() {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        return profile.getPatients().stream()
                .map(patientProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addPatientToCurrentDoctor(Long patientUserId) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        // Initialize collections if needed
        Set<PatientProfile> doctorPatients =
                doctorProfile.getPatients() != null ? doctorProfile.getPatients() : new HashSet<>();
        Set<DoctorProfile> patientDoctors =
                patientProfile.getDoctors() != null ? patientProfile.getDoctors() : new HashSet<>();

        // If already linked, do nothing (idempotent)
        if (doctorPatients.contains(patientProfile)) {
            doctorProfile.setPatients(doctorPatients);
            patientProfile.setDoctors(patientDoctors);
            return;
        }

        doctorPatients.add(patientProfile);
        patientDoctors.add(doctorProfile);

        doctorProfile.setPatients(doctorPatients);
        patientProfile.setDoctors(patientDoctors);

        // DoctorProfile is the owning side of the join table, saving it is enough.
        doctorProfileRepository.save(doctorProfile);
    }

    @Override
    @Transactional
    public void removePatientFromCurrentDoctor(Long patientUserId) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        Set<PatientProfile> doctorPatients =
                doctorProfile.getPatients() != null ? doctorProfile.getPatients() : new HashSet<>();
        Set<DoctorProfile> patientDoctors =
                patientProfile.getDoctors() != null ? patientProfile.getDoctors() : new HashSet<>();

        if (doctorPatients.remove(patientProfile)) {
            patientDoctors.remove(doctorProfile);

            doctorProfile.setPatients(doctorPatients);
            patientProfile.setDoctors(patientDoctors);

            doctorProfileRepository.save(doctorProfile);
        }
        // If not linked, silently ignore (idempotent).
    }
}
