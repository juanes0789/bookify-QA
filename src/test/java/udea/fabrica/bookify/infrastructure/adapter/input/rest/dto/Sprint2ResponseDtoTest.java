package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.LoginResponseDto;

class Sprint2ResponseDtoTest {

    @Test
    void shouldExposeBearerTokenData_whenAuthResponseIsBuilt() {
        // Arrange
        var builder = AuthResponse.builder().accessToken("token-firmado").tokenType("Bearer")
                .expiresIn(3600L).role(UserRole.PROVEEDOR);

        // Act
        AuthResponse response = builder.build();

        // Assert
        assertThat(response).isEqualTo(new AuthResponse("token-firmado", "Bearer", 3600L, UserRole.PROVEEDOR));
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getRole()).isEqualTo(UserRole.PROVEEDOR);
    }

    @Test
    void shouldReportBlockUntil_whenLoginIsRejectedByBlock() {
        // Arrange
        OffsetDateTime blockedUntil = OffsetDateTime.parse("2026-10-17T10:15:00-05:00");

        // Act
        LoginResponseDto response = new LoginResponseDto(false, true, blockedUntil);

        // Assert
        assertThat(response.authenticated()).isFalse();
        assertThat(response.blocked()).isTrue();
        assertThat(response.blockedUntil()).isEqualTo(blockedUntil);
    }
}
