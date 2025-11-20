package tn.esprit.docsbackend.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserDto {

    private Long id;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private String profileImage;

    private Boolean isFirstLogin;

    private Role role;

    private UserStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
