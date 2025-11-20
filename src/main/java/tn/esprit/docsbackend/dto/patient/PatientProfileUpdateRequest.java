package tn.esprit.docsbackend.dto.patient;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileUpdateRequest {

    private LocalDate dateOfBirth;

    @Size(max = 20)
    private String gender;

    @Size(max = 10)
    private String bloodType;

    private Integer heightCm;

    private Integer weightKg;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    @Size(max = 30)
    private String maritalStatus;

    private Boolean smoker;

    private Boolean alcoholUse;

    @Size(max = 1000)
    private String notes;
}
