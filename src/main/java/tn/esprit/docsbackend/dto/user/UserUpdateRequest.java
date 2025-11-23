package tn.esprit.docsbackend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Partial update for the base User fields.
 * All fields are optional; only non-null ones are applied.
 */
@Getter
@Setter
public class UserUpdateRequest {

    @Size(max = 50)
    private String firstname;

    @Size(max = 50)
    private String lastname;

    @Size(max = 20)
    private String phone;

    /**
     * Optional email update. If provided and different, we check uniqueness.
     */
    @Email
    @Size(max = 120)
    private String email;
}
