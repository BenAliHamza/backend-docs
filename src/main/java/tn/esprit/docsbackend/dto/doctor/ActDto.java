package tn.esprit.docsbackend.dto.doctor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ActDto {

    private Long id;

    private String code;

    private String name;

    private String description;

    private BigDecimal basePrice;

    private Integer defaultDurationMinutes;

    private Boolean teleconsultationAvailable;
}
