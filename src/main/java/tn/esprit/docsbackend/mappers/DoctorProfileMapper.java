package tn.esprit.docsbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.doctor.ActDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileDto;
import tn.esprit.docsbackend.dto.doctor.DoctorProfileUpdateRequest;
import tn.esprit.docsbackend.dto.doctor.SpecialtyDto;
import tn.esprit.docsbackend.entities.Act;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.entities.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DoctorProfileMapper {

    private final SpecialtyMapper specialtyMapper;
    private final ActMapper actMapper;

    public DoctorProfileDto toDto(DoctorProfile profile) {
        if (profile == null || profile.getUser() == null) {
            return null;
        }

        User user = profile.getUser();

        List<SpecialtyDto> specialties = null;
        if (profile.getSpecialties() != null) {
            specialties = profile.getSpecialties().stream()
                    .filter(Objects::nonNull)
                    .map(specialtyMapper::toDto)
                    .collect(Collectors.toList());
        }

        List<ActDto> acts = null;
        if (profile.getActs() != null) {
            acts = profile.getActs().stream()
                    .filter(Objects::nonNull)
                    .map(actMapper::toDto)
                    .collect(Collectors.toList());
        }

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
                .specialties(specialties)
                .acts(acts)
                .build();
    }

    /**
     * Applies a partial update from the request onto the existing entity.
     * Only non-null fields are updated.
     */
    public void updateEntityFromRequest(DoctorProfileUpdateRequest request, DoctorProfile profile) {
        if (request == null || profile == null) {
            return;
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getYearsOfExperience() != null) {
            profile.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getClinicAddress() != null) {
            profile.setClinicAddress(request.getClinicAddress());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getMedicalRegistrationNumber() != null) {
            profile.setMedicalRegistrationNumber(request.getMedicalRegistrationNumber());
        }
        if (request.getConsultationFee() != null) {
            profile.setConsultationFee(request.getConsultationFee());
        }
        if (request.getAcceptsNewPatients() != null) {
            profile.setAcceptsNewPatients(request.getAcceptsNewPatients());
        }
        if (request.getTeleconsultationEnabled() != null) {
            profile.setTeleconsultationEnabled(request.getTeleconsultationEnabled());
        }
        if (request.getMaxDailyAppointments() != null) {
            profile.setMaxDailyAppointments(request.getMaxDailyAppointments());
        }
        if (request.getAverageConsultationDurationMinutes() != null) {
            profile.setAverageConsultationDurationMinutes(request.getAverageConsultationDurationMinutes());
        }
    }
}
