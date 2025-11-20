package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.services.UserImageService;

/**
 * Endpoints related specifically to user profile images.
 */
@RestController
@RequiredArgsConstructor
public class UserImageController {

    private final UserImageService userImageService;

    /**
     * Upload or replace the current user's profile image.
     *
     * Endpoint:
     *   POST /users/me/profile-image
     *
     * Request:
     *   multipart/form-data with field "image"
     *
     * Response:
     *   UserDto (with updated profileImage URL)
     */
    @PostMapping("/users/me/profile-image")
    public ResponseEntity<UserDto> uploadMyProfileImage(
            @RequestParam("image") MultipartFile image
    ) {
        UserDto updatedUser = userImageService.uploadCurrentUserProfileImage(image);
        return ResponseEntity.ok(updatedUser);
    }
}
