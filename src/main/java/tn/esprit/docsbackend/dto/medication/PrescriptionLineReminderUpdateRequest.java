package tn.esprit.docsbackend.dto.medication;

import lombok.Data;

@Data
public class PrescriptionLineReminderUpdateRequest {

    private Boolean reminderEnabled;
}
