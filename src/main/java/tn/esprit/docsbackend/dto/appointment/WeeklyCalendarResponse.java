package tn.esprit.docsbackend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeeklyCalendarResponse {
    private List<DailyScheduleDto> days;
}
