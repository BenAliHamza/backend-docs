package tn.esprit.docsbackend.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean active;
}
