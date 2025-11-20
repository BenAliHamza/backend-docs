package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.doctor.ActDto;
import tn.esprit.docsbackend.entities.Act;

@Component
public class ActMapper {

    public ActDto toDto(Act act) {
        if (act == null) {
            return null;
        }

        return ActDto.builder()
                .id(act.getId())
                .code(act.getCode())
                .name(act.getName())
                .description(act.getDescription())
                .basePrice(act.getBasePrice())
                .defaultDurationMinutes(act.getDefaultDurationMinutes())
                .teleconsultationAvailable(act.getTeleconsultationAvailable())
                .build();
    }
}
