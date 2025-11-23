// src/main/java/tn/esprit/docsbackend/dto/appointment/AppointmentBookingRequest.java
package tn.esprit.docsbackend.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentBookingRequest {

    @NotNull
    private Long slotId;

    private String reason; // optional, for later
}
