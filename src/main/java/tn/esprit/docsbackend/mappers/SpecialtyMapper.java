package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.doctor.SpecialtyDto;
import tn.esprit.docsbackend.entities.Specialty;

@Component
public class SpecialtyMapper {

    public SpecialtyDto toDto(Specialty specialty) {
        if (specialty == null) {
            return null;
        }

        return SpecialtyDto.builder()
                .id(specialty.getId())
                .code(specialty.getCode())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .active(specialty.getActive())
                .build();
    }
}
