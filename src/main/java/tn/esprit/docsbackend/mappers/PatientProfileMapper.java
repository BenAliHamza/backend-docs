package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;

@Component
public class PatientProfileMapper {

    public PatientProfileDto toDto(PatientProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();
        if (user == null) {
            return null;
        }

        return PatientProfileDto.builder()
                .id(profile.getId())
                .userId(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .bloodType(profile.getBloodType())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .build();
    }
}
