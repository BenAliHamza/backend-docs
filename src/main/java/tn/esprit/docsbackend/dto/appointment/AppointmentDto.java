package tn.esprit.docsbackend.dto.appointment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder

public class AppointmentDto {

    private Long id;

    private Long doctorUserId;
    private String doctorFullName;

    private Long patientUserId;
    private String patientFullName;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private AppointmentStatus status;

    private String reason;
}
