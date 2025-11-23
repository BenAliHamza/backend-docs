package tn.esprit.docsbackend.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.docsbackend.entities.enums.RecurrenceType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DoctorProfile doctorProfile;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private Integer slotDurationMinutes;

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    // NEW: store selected days (for WEEKLY)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "availability_session_days",
            joinColumns = @JoinColumn(name = "availability_session_id")
    )
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> daysOfWeek;
}
