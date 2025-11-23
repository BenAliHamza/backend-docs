package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.user.ChangePasswordRequest;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.dto.user.UserUpdateRequest;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.mappers.UserMapper;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.UserService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();
        return userMapper.toDto(currentUser);
    }

    @Override
    @Transactional
    public UserDto updateCurrentUser(UserUpdateRequest request) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        // firstname
        if (request.getFirstname() != null) {
            currentUser.setFirstname(request.getFirstname());
        }

        // lastname
        if (request.getLastname() != null) {
            currentUser.setLastname(request.getLastname());
        }

        // phone
        if (request.getPhone() != null) {
            currentUser.setPhone(request.getPhone());
        }

        // email (optional, with uniqueness check)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();

            if (!newEmail.equalsIgnoreCase(currentUser.getEmail())) {
                // check if any *other* user already uses this email (and is not soft-deleted)
                Optional<User> existing = userRepository.findByEmailAndDeletedFalse(newEmail);
                if (existing.isPresent() && !existing.get().getId().equals(currentUser.getId())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Email is already in use by another account"
                    );
                }
                currentUser.setEmail(newEmail);
            }
        }

        User saved = userRepository.save(currentUser);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        // Check current password
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        // Prevent reusing the same password
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from current password");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // If you consider "first login" finished when user changes password, you can flip the flag:
        if (Boolean.TRUE.equals(currentUser.getIsFirstLogin())) {
            currentUser.setIsFirstLogin(false);
        }

        userRepository.save(currentUser);

        // Optional: you *could* invalidate refresh tokens here if you want strict logout on password change.
        // That would require using RefreshTokenRepository, which we are not wiring in here to keep it simple.
    }
}
