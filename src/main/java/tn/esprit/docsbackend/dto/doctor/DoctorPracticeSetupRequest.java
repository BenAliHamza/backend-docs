package tn.esprit.docsbackend.dto.doctor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request body for doctor onboarding step where they choose
 * their main specialty and the acts they perform.
 */
@Getter
@Setter
public class DoctorPracticeSetupRequest {

    @NotNull
    private Long specialtyId;

    @NotEmpty
    private List<Long> actIds;
}
