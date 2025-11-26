package tn.esprit.docsbackend.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple weekly schedule slot for a doctor.
 * dayOfWeek: "MONDAY", "TUESDAY", ...
 * startTime / endTime: "HH:mm"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleDto {

    private Long id;
    private String dayOfWeek;  // e.g. "MONDAY"
    private String startTime;  // e.g. "08:00"
    private String endTime;    // e.g. "12:00"
    private Boolean active;
}
