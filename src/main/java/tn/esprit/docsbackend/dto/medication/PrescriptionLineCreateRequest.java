package tn.esprit.docsbackend.dto.medication;

import lombok.Data;

@Data
public class PrescriptionLineCreateRequest {

    private Long medicationId;
    private String dosage;
    private Integer timesPerDay;
    private String instructions;
}
