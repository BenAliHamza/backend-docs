package tn.esprit.docsbackend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response carrying access and refresh tokens.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;

    private String refreshToken;

    /**
     * Usually "Bearer".
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token validity in seconds (optional, for client UX).
     */
    private Long expiresIn;
}
