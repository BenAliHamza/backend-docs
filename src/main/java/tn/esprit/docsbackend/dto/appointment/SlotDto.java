package tn.esprit.docsbackend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlotDto {
    private String time;      // "08:30"
    private boolean available;
}
