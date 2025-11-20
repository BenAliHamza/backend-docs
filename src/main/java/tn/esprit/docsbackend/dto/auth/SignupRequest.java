package tn.esprit.docsbackend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.docsbackend.entities.enums.Role;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    @Size(max = 50)
    private String firstname;

    @NotBlank
    @Size(max = 50)
    private String lastname;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    /**
     * Role to create: DOCTOR or PATIENT.
     * ADMIN users should be created via admin flows, not public signup.
     */
    @NotNull
    private Role role;
}
