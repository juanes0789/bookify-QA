package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String role;

    public AuthResponse(String accessToken, String tokenType, Long expiresIn, String role) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.role = role;
    }
}