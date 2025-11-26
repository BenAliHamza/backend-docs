package tn.esprit.docsbackend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionLineDto {

    private Long id;

    private Long prescriptionId;
    private LocalDate prescriptionStartDate;
    private LocalDate prescriptionEndDate;

    private Long medicationId;
    private String medicationName;

    private String dosage;
    private Integer timesPerDay;
    private String instructions;

    private Boolean reminderEnabled;
}
