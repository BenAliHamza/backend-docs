package tn.esprit.docsbackend.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "appointments")

public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    private DoctorProfile doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patient;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "notes", length = 1000)
    private String notes;
}
