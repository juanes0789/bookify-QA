package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import udea.fabrica.bookify.domain.model.UserRole;

@Data
@Builder
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserRole role;

    public AuthResponse(String accessToken, String tokenType, Long expiresIn, UserRole role) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.role = role;
    }
}