package tn.esprit.docsbackend.dto.doctor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class DoctorProfileDto {

    private Long id;

    private Long userId;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private String bio;

    private Integer yearsOfExperience;

    private String clinicAddress;

    private String city;

    private String country;

    private String medicalRegistrationNumber;

    private BigDecimal consultationFee;

    private Boolean acceptsNewPatients;

    private Boolean teleconsultationEnabled;

    private Integer maxDailyAppointments;

    private Integer averageConsultationDurationMinutes;

    /**
     * Single main specialty of this doctor.
     */
    private SpecialtyDto specialty;

    /**
     * Acts/services this doctor offers.
     */
    private List<ActDto> acts;
}
