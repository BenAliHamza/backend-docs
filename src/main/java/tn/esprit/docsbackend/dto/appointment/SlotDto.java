// src/main/java/tn/esprit/docsbackend/dto/appointment/SlotDto.java
package tn.esprit.docsbackend.dto.appointment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.docsbackend.entities.enums.SlotStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SlotDto {

    private Long id;
    private Long doctorProfileId;
    private Long patientProfileId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private SlotStatus status;
}
