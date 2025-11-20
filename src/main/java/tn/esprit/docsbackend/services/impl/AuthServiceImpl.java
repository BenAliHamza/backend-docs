package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.auth.LoginRequest;
import tn.esprit.docsbackend.dto.auth.RefreshTokenRequest;
import tn.esprit.docsbackend.dto.auth.SignupRequest;
import tn.esprit.docsbackend.dto.auth.TokenResponse;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.RefreshToken;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.RefreshTokenRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.AuthService;
import tn.esprit.docsbackend.utils.JwtTokenProvider;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenResponse signup(SignupRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sign up as ADMIN");
        }

        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .isFirstLogin(true)
                .build();

        userRepository.save(user);

        // Create associated profile based on role
        if (request.getRole() == Role.DOCTOR) {
            DoctorProfile doctorProfile = DoctorProfile.builder()
                    .user(user)
                    .build();
            doctorProfileRepository.save(doctorProfile);
        } else if (request.getRole() == Role.PATIENT) {
            PatientProfile patientProfile = PatientProfile.builder()
                    .user(user)
                    .build();
            patientProfileRepository.save(patientProfile);
        }

        return generateTokensForUser(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return generateTokensForUser(user);
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        String providedToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(providedToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(providedToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
        }

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndDeletedFalse(providedToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found or revoked"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        User user = storedToken.getUser();
        if (user == null || Boolean.TRUE.equals(user.isDeleted())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User for this token no longer exists");
        }

        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        // Rotate refresh token: revoke old, issue new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateTokensForUser(user);
    }

    private TokenResponse generateTokensForUser(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refreshExpiry = now.plusSeconds(jwtTokenProvider.getRefreshTokenValidityMillis() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(refreshExpiry)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        long expiresInSeconds = jwtTokenProvider.getAccessTokenValidityMillis() / 1000;

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(expiresInSeconds)
                .build();
    }
}
