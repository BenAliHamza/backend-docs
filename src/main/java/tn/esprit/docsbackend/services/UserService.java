package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.user.UserDto;

public interface UserService {

    /**
     * Returns the currently authenticated user's profile information.
     */
    UserDto getCurrentUser();
}
