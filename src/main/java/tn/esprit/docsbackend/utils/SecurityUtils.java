package tn.esprit.docsbackend.utils;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;

/**
 * Security-related helper functions.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // utility class
    }

    /**
     * Returns the currently authenticated User or throws 401 if not authenticated.
     */
    public static User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        // If principal is not our User entity, consider this unauthorized for now.
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User principal is not available");
    }

    /**
     * Ensures the current user has the given role, otherwise throws 403.
     */
    public static User getCurrentUserWithRoleOrThrow(Role requiredRole) {
        User user = getCurrentUserOrThrow();
        if (user.getRole() != requiredRole) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied. Required role: " + requiredRole
            );
        }
        return user;
    }
}
