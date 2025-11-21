package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.entities.Act;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.mappers.DoctorProfileMapper;
import tn.esprit.docsbackend.mappers.PatientProfileMapper;
import tn.esprit.docsbackend.repositories.ActRepository;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.SpecialtyRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final ActRepository actRepository;
    private final SpecialtyRepository specialtyRepository;
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
     * On first successful update, marks user.isFirstLogin = false.
     */
    @Override
    @Transactional
    public DoctorProfileDto updateCurrentDoctorProfile(DoctorProfileUpdateRequest request) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        // Apply the partial update via mapper
        doctorProfileMapper.updateEntityFromRequest(request, profile);

        DoctorProfile saved = doctorProfileRepository.save(profile);

        // If this is the first time completing/updating profile, flip the flag
        if (Boolean.TRUE.equals(currentUser.getIsFirstLogin())) {
            currentUser.setIsFirstLogin(false);
            userRepository.save(currentUser);
        }

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

        Set<PatientProfile> doctorPatients =
                doctorProfile.getPatients() != null ? doctorProfile.getPatients() : new HashSet<>();
        Set<DoctorProfile> patientDoctors =
                patientProfile.getDoctors() != null ? patientProfile.getDoctors() : new HashSet<>();

        if (doctorPatients.contains(patientProfile)) {
            doctorProfile.setPatients(doctorPatients);
            patientProfile.setDoctors(patientDoctors);
            return;
        }

        doctorPatients.add(patientProfile);
        patientDoctors.add(doctorProfile);

        doctorProfile.setPatients(doctorPatients);
        patientProfile.setDoctors(patientDoctors);

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
    }

    @Override
    @Transactional
    public DoctorProfileDto setupPracticeForCurrentDoctor(Long specialtyId, List<Long> actIds) {
        if (specialtyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "specialtyId is required");
        }
        if (actIds == null || actIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one actId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Specialty specialty = specialtyRepository.findByIdAndDeletedFalse(specialtyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialty not found"));

        // For onboarding: set the single main specialty
        doctorProfile.setSpecialty(specialty);

        // Load acts by id
        List<Act> acts = actRepository.findAllById(actIds);

        if (acts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No acts found for given ids");
        }

        int distinctRequestedIds = new java.util.HashSet<>(actIds).size();
        if (acts.size() != distinctRequestedIds) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some act ids are invalid");
        }

        for (Act act : acts) {
            if (act == null || act.isDeleted()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the acts is not available");
            }
            if (act.getDoctor() == null || !doctorProfile.getId().equals(act.getDoctor().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Act " + act.getId() + " does not belong to current doctor");
            }
            if (act.getSpecialty() == null || !specialtyId.equals(act.getSpecialty().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Act " + act.getId() + " does not belong to selected specialty");
            }
        }

        // Replace the doctor's acts with the selected ones (onboarding setup)
        java.util.Set<Act> doctorActs = doctorProfile.getActs() != null
                ? doctorProfile.getActs()
                : new java.util.HashSet<>();
        doctorActs.clear();
        doctorActs.addAll(acts);
        doctorProfile.setActs(doctorActs);

        DoctorProfile saved = doctorProfileRepository.save(doctorProfile);

        return doctorProfileMapper.toDto(saved);
    }

}
