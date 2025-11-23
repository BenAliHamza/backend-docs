package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.user.ChangePasswordRequest;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.dto.user.UserUpdateRequest;

public interface UserService {

    /**
     * Returns the currently authenticated user as a DTO.
     */
    UserDto getCurrentUser();

    /**
     * Partially update base user fields (firstname, lastname, phone, email).
     */
    UserDto updateCurrentUser(UserUpdateRequest request);

    /**
     * Change the password of the currently authenticated user.
     */
    void changePassword(ChangePasswordRequest request);
}
