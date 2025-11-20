package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.entities.User;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .isFirstLogin(user.getIsFirstLogin())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
