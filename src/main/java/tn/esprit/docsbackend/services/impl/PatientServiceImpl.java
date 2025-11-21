package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.dto.patient.PatientProfileUpdateRequest;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.mappers.PatientProfileMapper;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.PatientService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final PatientProfileMapper patientProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public PatientProfileDto getCurrentPatientProfile() {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        if (currentUser.getRole() != Role.PATIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can access this resource");
        }

        PatientProfile profile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        return patientProfileMapper.toDto(profile);
    }

    /**
     * Update the profile of the currently authenticated patient (partial update).
     * On first successful update, marks user.isFirstLogin = false.
     */
    @Override
    @Transactional
    public PatientProfileDto updateCurrentPatientProfile(PatientProfileUpdateRequest request) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile profile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getBloodType() != null) {
            profile.setBloodType(request.getBloodType());
        }
        if (request.getHeightCm() != null) {
            profile.setHeightCm(request.getHeightCm());
        }
        if (request.getWeightKg() != null) {
            profile.setWeightKg(request.getWeightKg());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getMaritalStatus() != null) {
            profile.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getSmoker() != null) {
            profile.setSmoker(request.getSmoker());
        }
        if (request.getAlcoholUse() != null) {
            profile.setAlcoholUse(request.getAlcoholUse());
        }
        if (request.getNotes() != null) {
            profile.setNotes(request.getNotes());
        }

        PatientProfile saved = patientProfileRepository.save(profile);

        // If this is the first time completing/updating profile, flip the flag
        if (Boolean.TRUE.equals(currentUser.getIsFirstLogin())) {
            currentUser.setIsFirstLogin(false);
            userRepository.save(currentUser);
        }

        return patientProfileMapper.toDto(saved);
    }

    /**
     * Returns the list of doctors linked to the currently authenticated patient.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileDto> getDoctorsOfCurrentPatient() {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile profile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        return profile.getDoctors().stream()
                .map(this::mapDoctorToDto)
                .collect(Collectors.toList());
    }

    private DoctorProfileDto mapDoctorToDto(DoctorProfile profile) {
        if (profile == null || profile.getUser() == null) {
            return null;
        }

        User user = profile.getUser();

        return DoctorProfileDto.builder()
                .id(profile.getId())
                .userId(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(profile.getBio())
                .yearsOfExperience(profile.getYearsOfExperience())
                .clinicAddress(profile.getClinicAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .medicalRegistrationNumber(profile.getMedicalRegistrationNumber())
                .consultationFee(profile.getConsultationFee())
                .acceptsNewPatients(profile.getAcceptsNewPatients())
                .teleconsultationEnabled(profile.getTeleconsultationEnabled())
                .maxDailyAppointments(profile.getMaxDailyAppointments())
                .averageConsultationDurationMinutes(profile.getAverageConsultationDurationMinutes())
                // we deliberately don't include specialty/acts here to keep it light;
                // you can wire in DoctorProfileMapper later if you want full details.
                .build();
    }
}
