package tn.esprit.docsbackend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentStatusUpdateRequest {
    private String status; // ACCEPTED / REJECTED / COMPLETED
}
