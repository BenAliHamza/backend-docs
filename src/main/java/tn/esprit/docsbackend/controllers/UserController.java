package tn.esprit.docsbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.dto.user.ChangePasswordRequest;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.dto.user.UserUpdateRequest;
import tn.esprit.docsbackend.services.UserService;

/**
 * Endpoints related to the base User entity (common to doctors & patients).
 *
 * NOTE:
 * - Doctor-specific fields: use DoctorController / DoctorProfile endpoints.
 * - Patient-specific fields: use PatientController / PatientProfile endpoints.
 */
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get info about the currently authenticated user.
     */
    @GetMapping("/me")
    public UserDto getCurrentUser() {
        return userService.getCurrentUser();
    }

    /**
     * Partially update base user fields (firstname, lastname, phone, email) for the current user.
     */
    @PutMapping("/me")
    public UserDto updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return userService.updateCurrentUser(request);
    }

    /**
     * Change password for the current user.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // If you already had other endpoints in this controller (admin list users, get by id, etc.),
    // just merge them back into this class. The important part here is the /me and /change-password endpoints.
}
