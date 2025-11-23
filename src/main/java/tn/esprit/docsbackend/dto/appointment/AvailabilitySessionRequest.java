package tn.esprit.docsbackend.dto.appointment;

import lombok.Data;
import tn.esprit.docsbackend.entities.enums.RecurrenceType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AvailabilitySessionRequest {

    // date range
    private LocalDate startDate;
    private LocalDate endDate;          // can be null → default to startDate

    // time range
    private LocalTime startTime;
    private LocalTime endTime;

    // slot duration (minutes)
    private Integer slotDurationMinutes;

    // NEW: recurrence mode
    private RecurrenceType recurrenceType;   // ONE_TIME or WEEKLY

    // NEW: days of week for WEEKLY
    // JSON from Android: ["MONDAY","WEDNESDAY",...]
    private List<DayOfWeek> daysOfWeek;
}
