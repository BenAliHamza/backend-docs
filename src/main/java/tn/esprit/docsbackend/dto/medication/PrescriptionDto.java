package tn.esprit.docsbackend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {

    private Long id;

    private Long doctorId;
    private Long doctorUserId;
    private String doctorFirstName;
    private String doctorLastName;

    private Long patientId;
    private Long patientUserId;
    private String patientFirstName;
    private String patientLastName;

    private LocalDate startDate;
    private LocalDate endDate;

    private String note;

    private List<PrescriptionLineDto> lines;
}
