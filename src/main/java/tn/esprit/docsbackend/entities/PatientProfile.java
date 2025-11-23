package tn.esprit.docsbackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "patient_profiles")
public class PatientProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "blood_type", length = 10)
    private String bloodType;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    /**
     * Address information for the patient.
     */
    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    /**
     * Optional marital status (single, married, etc.).
     */
    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    /**
     * Lifestyle flags.
     */
    @Column(name = "smoker")
    private Boolean smoker;

    @Column(name = "alcohol_use")
    private Boolean alcoholUse;

    /**
     * Free-form notes / summary for clinicians.
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Many-to-many relationship between patients and doctors.
     * This side is the inverse of DoctorProfile.patients.
     */
    @ManyToMany(mappedBy = "patients")
    @Builder.Default
    private Set<DoctorProfile> doctors = new HashSet<>();
}
