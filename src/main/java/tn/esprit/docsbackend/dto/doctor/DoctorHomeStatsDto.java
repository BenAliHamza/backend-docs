package tn.esprit.docsbackend.dto.doctor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorHomeStatsDto {
    private long todayAppointments;
    private long weekAppointments;
    private long totalPatients;
    private String nextAppointmentStart;
    private String nextAppointmentPatientName;
}
