package tn.esprit.docsbackend.dto.medication;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionCreateRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private List<PrescriptionLineCreateRequest> lines;
}
