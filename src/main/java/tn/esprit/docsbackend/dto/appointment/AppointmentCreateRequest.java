package tn.esprit.docsbackend.dto.appointment;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Patient books an appointment with a doctor.
 */
@Data
public class AppointmentCreateRequest {

    private Long doctorId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String reason;
    private Boolean teleconsultation;
}
