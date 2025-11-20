package tn.esprit.docsbackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utility for creating and validating JWT access and refresh tokens.
 * For now the secret and expirations are hardcoded; you can move them to properties later.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "type";

    private final Key signingKey;

    // 15 minutes for access token
    private final long accessTokenValidityMillis = 15 * 60 * 1000L;

    // 7 days for refresh token
    private final long refreshTokenValidityMillis = 7L * 24 * 60 * 60 * 1000L;

    public JwtTokenProvider() {
        // In production, move this to configuration
        String secret = "change-this-secret-to-a-very-long-random-string-change-this";
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole() != null ? user.getRole().name() : Role.PATIENT.name())
                .claim(CLAIM_TOKEN_TYPE, "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole() != null ? user.getRole().name() : Role.PATIENT.name())
                .claim(CLAIM_TOKEN_TYPE, "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token).getBody();
        String subject = claims.getSubject();
        if (subject == null) {
            return null;
        }
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String getTokenType(String token) {
        Claims claims = parseClaims(token).getBody();
        Object value = claims.get(CLAIM_TOKEN_TYPE);
        return value != null ? value.toString() : null;
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token).getBody();
        Object value = claims.get(CLAIM_EMAIL);
        return value != null ? value.toString() : null;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }

    public long getAccessTokenValidityMillis() {
        return accessTokenValidityMillis;
    }

    public long getRefreshTokenValidityMillis() {
        return refreshTokenValidityMillis;
    }
}
