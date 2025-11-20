package tn.esprit.docsbackend.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.docsbackend.dto.user.UserDto;

/**
 * Handles user profile image operations (upload, update).
 */
public interface UserImageService {

    /**
     * Upload a new profile image for the currently authenticated user,
     * store the resulting URL in the user's profileImage field,
     * and return the updated user DTO.
     */
    UserDto uploadCurrentUserProfileImage(MultipartFile imageFile);
}
