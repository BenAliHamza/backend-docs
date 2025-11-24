package tn.esprit.docsbackend.dto.indicator;

import lombok.Data;

@Data
public class IndicatorTypeDto {
    private Long id;
    private String code;
    private String name;
    private String unit;
    private String description;
    private Boolean active;
}
