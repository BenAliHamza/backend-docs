package tn.esprit.docsbackend.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public-facing doctor profile for patients/search.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorPublicProfileDto {

    private Long doctorId;
    private Long userId;

    // Basic identity
    private String firstName;
    private String lastName;
    private String profileImageUrl;

    // Professional info
    private String specialtyName;
    private Long specialtyId;
    private String city;
    private String country;
    private String clinicAddress;
    private String bio;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private Boolean acceptingNewPatients;
    private Boolean teleconsultationEnabled;

    // Acts
    private List<ActDto> acts; // reuse existing dto.doctor.ActDto
}
