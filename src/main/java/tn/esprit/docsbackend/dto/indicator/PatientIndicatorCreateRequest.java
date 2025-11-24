package tn.esprit.docsbackend.dto.indicator;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request body when a patient records a new measurement.
 */
@Data
public class PatientIndicatorCreateRequest {

    private Long indicatorTypeId;          // required

    private BigDecimal numericValue;       // at least one of numeric/text must be provided
    private String textValue;

    /**
     * Optional custom timestamp. If null, backend will use now().
     */
    private LocalDateTime measuredAt;

    private String note;
}
