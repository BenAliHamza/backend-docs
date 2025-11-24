package tn.esprit.docsbackend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "patient_indicators")
public class PatientIndicator extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "indicator_type_id", nullable = false)
    private IndicatorType indicatorType;

    /**
     * Numeric value when it makes sense (e.g. 120, 80, 5.6, etc.).
     * Optional, because some indicators might be textual only.
     */
    @Column(name = "numeric_value", precision = 10, scale = 2)
    private BigDecimal numericValue;

    /**
     * Free-form value, e.g. "Positive", "Moderate", or complex text.
     */
    @Column(name = "text_value", length = 255)
    private String textValue;

    /**
     * When this measurement was taken.
     * We will treat this as "last updated time" of this measurement.
     */
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    /**
     * Optional short note the patient can add.
     */
    @Column(name = "note", length = 500)
    private String note;
}
