// src/main/java/tn/esprit/docsbackend/entities/AvailabilitySession.java
package tn.esprit.docsbackend.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.docsbackend.entities.enums.RecurrenceType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "availability_sessions")
public class AvailabilitySession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    private DoctorProfile doctorProfile;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * If null → same as startDate.
     * Used for WEEKLY recurrence.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 20)
    private RecurrenceType recurrenceType;
}
