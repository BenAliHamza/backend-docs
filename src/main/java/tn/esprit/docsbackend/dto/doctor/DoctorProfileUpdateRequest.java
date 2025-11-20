package tn.esprit.docsbackend.dto.doctor;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorProfileUpdateRequest {

    @Size(max = 1000)
    private String bio;

    private Integer yearsOfExperience;

    @Size(max = 255)
    private String clinicAddress;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String medicalRegistrationNumber;

    private BigDecimal consultationFee;

    /**
     * Whether this doctor currently accepts new patients.
     */
    private Boolean acceptsNewPatients;

    /**
     * Whether the doctor offers teleconsultation / remote visits.
     */
    private Boolean teleconsultationEnabled;

    /**
     * Soft limit for how many appointments per day this doctor usually takes.
     */
    private Integer maxDailyAppointments;

    /**
     * Default consultation duration in minutes.
     */
    private Integer averageConsultationDurationMinutes;
}
