package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.auth.LoginRequest;
import tn.esprit.docsbackend.dto.auth.RefreshTokenRequest;
import tn.esprit.docsbackend.dto.auth.SignupRequest;
import tn.esprit.docsbackend.dto.auth.TokenResponse;

public interface AuthService {

    /**
     * Register a new user (DOCTOR or PATIENT).
     */
    TokenResponse signup(SignupRequest request);

    /**
     * Authenticate user and return access + refresh tokens.
     */
    TokenResponse login(LoginRequest request);

    /**
     * Use a refresh token to obtain new access (and possibly refresh) tokens.
     */
    TokenResponse refresh(RefreshTokenRequest request);
}
