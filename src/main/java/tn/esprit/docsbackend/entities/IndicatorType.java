package tn.esprit.docsbackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "indicator_types")
public class IndicatorType extends BaseEntity {

    /**
     * Short code, e.g. "HR", "BP_SYS", "BP_DIA", "GLUCOSE_FASTING".
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * User-facing name, e.g. "Heart rate", "Systolic blood pressure".
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Measurement unit, e.g. "bpm", "mmHg", "mg/dL".
     */
    @Column(name = "unit", length = 50)
    private String unit;

    /**
     * Optional description / hints on how to measure it.
     */
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}
