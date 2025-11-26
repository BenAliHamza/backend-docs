package tn.esprit.docsbackend.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {

    private Long id;

    private Long doctorId;
    private Long doctorUserId;
    private String doctorFirstName;
    private String doctorLastName;

    private Long patientId;
    private Long patientUserId;
    private String patientFirstName;
    private String patientLastName;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private AppointmentStatus status;

    private String reason;
    private Boolean teleconsultation;
}
