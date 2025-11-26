package tn.esprit.docsbackend.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "prescription_lines")
@ToString
public class PrescriptionLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    /**
     * Example: "1 tablet", "10 ml", etc.
     */
    @Column(name = "dosage", length = 100)
    private String dosage;

    /**
     * How many times per day the patient should take this medication.
     */
    @Column(name = "times_per_day")
    private Integer timesPerDay;

    /**
     * Free-form instructions: "After meals", "Before sleep", etc.
     */
    @Column(name = "instructions", length = 1000)
    private String instructions;

    /**
     * Whether the patient wants reminders for this medication.
     */
    @Column(name = "reminder_enabled")
    private Boolean reminderEnabled;
}
