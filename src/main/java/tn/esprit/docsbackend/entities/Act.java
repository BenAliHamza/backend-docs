package tn.esprit.docsbackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "acts")
public class Act extends BaseEntity {

    /**
     * Owning doctor of this act.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    /**
     * Specialty this act is associated with.
     * This lets us filter acts by specialty during onboarding.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Base price for this act. May override the doctor's generic consultation fee.
     */
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * Typical duration of this act in minutes.
     */
    @Column(name = "default_duration_min")
    private Integer defaultDurationMinutes;

    /**
     * Whether this act can be done via teleconsultation.
     */
    @Column(name = "teleconsultation_available")
    private Boolean teleconsultationAvailable;
}
