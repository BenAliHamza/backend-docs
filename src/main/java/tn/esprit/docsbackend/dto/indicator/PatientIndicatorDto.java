package tn.esprit.docsbackend.dto.indicator;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PatientIndicatorDto {

    private Long id;

    private Long indicatorTypeId;
    private String indicatorCode;
    private String indicatorName;
    private String unit;

    private BigDecimal numericValue;
    private String textValue;

    private LocalDateTime measuredAt;

    private String note;
}
