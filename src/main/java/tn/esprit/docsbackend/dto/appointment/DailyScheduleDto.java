package tn.esprit.docsbackend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DailyScheduleDto {
    private LocalDate date;
    private List<SlotDto> slots;
}
