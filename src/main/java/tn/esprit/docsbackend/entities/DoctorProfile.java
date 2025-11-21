package tn.esprit.docsbackend.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doctor_profiles")
public class DoctorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    /**
     * Primary clinic address for this doctor.
     */
    @Column(name = "clinic_address", length = 255)
    private String clinicAddress;

    /**
     * City where the doctor mainly practices.
     */
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Country where the doctor mainly practices.
     */
    @Column(name = "country", length = 100)
    private String country;

    /**
     * Medical registration number / license number.
     */
    @Column(name = "medical_registration_number", length = 100)
    private String medicalRegistrationNumber;

    /**
     * Base consultation fee used when no act-specific fee is provided.
     */
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    /**
     * Whether this doctor currently accepts new patients.
     */
    @Column(name = "accepts_new_patients")
    private Boolean acceptsNewPatients;

    /**
     * Whether the doctor offers teleconsultation / remote visits.
     */
    @Column(name = "teleconsultation_enabled")
    private Boolean teleconsultationEnabled;

    /**
     * Soft limit for how many appointments per day this doctor usually takes.
     */
    @Column(name = "max_daily_appointments")
    private Integer maxDailyAppointments;

    /**
     * Default consultation duration in minutes.
     */
    @Column(name = "avg_consultation_duration_min")
    private Integer averageConsultationDurationMinutes;

    /**
     * Many-to-many relationship between doctors and patients.
     * This side owns the join table.
     */
    @ManyToMany
    @JoinTable(
            name = "doctor_patient_links",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    @Builder.Default
    private Set<PatientProfile> patients = new HashSet<>();

    /**
     * Each doctor has exactly one main specialty.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    /**
     * One-to-many relationship between a doctor and their acts.
     */
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Act> acts = new HashSet<>();
}
