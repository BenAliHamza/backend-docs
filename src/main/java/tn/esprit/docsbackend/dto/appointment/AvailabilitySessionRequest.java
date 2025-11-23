// src/main/java/tn/esprit/docsbackend/dto/appointment/AvailabilitySessionRequest.java
package tn.esprit.docsbackend.dto.appointment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.docsbackend.entities.enums.RecurrenceType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AvailabilitySessionRequest {

    @NotNull
    private LocalDate startDate;

    /**
     * Optional; if null → only startDate.
     * For WEEKLY recurrence, this is the end of the period.
     */
    private LocalDate endDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    @Min(5)
    private Integer slotDurationMinutes;

    @NotNull
    private RecurrenceType recurrenceType;
}
