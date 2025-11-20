package tn.esprit.docsbackend.dto.patient;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PatientProfileDto {

    private Long id;

    private Long userId;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    private String gender;

    private String bloodType;

    private Integer heightCm;

    private Integer weightKg;

    private String address;

    private String city;

    private String country;

    private String maritalStatus;

    private Boolean smoker;

    private Boolean alcoholUse;

    private String notes;
}
