package udea.fabrica.bookify.infrastructure.config;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import udea.fabrica.bookify.domain.model.UserRole;

class JwtServiceTest {
    private static final String SECRET = "claveDePruebaParaBookifyConMasDeTreintaYDosBytes2026";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
    }

    @Test
    void shouldIncludeUserIdEmailAndRole_whenTokenIsGenerated() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        String token = jwtService.generateToken(userId, "carlos@barberia.com", UserRole.PROVEEDOR);

        // Assert
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("carlos@barberia.com");
        assertThat(claims.get("role", String.class)).isEqualTo("PROVEEDOR");
    }

    @Test
    void shouldExpireTokenInSixtyMinutes_whenTokenIsGenerated() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        String token = jwtService.generateToken(userId, "ana@mail.com", UserRole.CLIENTE);

        // Assert
        Claims claims = jwtService.extractClaims(token);
        long lifetimeMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(lifetimeMillis).isBetween(3_599_000L, 3_601_000L);
    }

    @Test
    void shouldRejectToken_whenSignedWithAnotherKey() {
        // Arrange
        JwtService otherService = new JwtService();
        ReflectionTestUtils.setField(otherService, "secretKey", "otraClaveDistintaParaFirmarTokensDeBookify2026");
        String foreignToken = otherService.generateToken(UUID.randomUUID(), "ana@mail.com", UserRole.CLIENTE);

        // Act / Assert
        assertThatThrownBy(() -> jwtService.extractClaims(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectToken_whenItIsExpired() {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject(UUID.randomUUID().toString())
                .claim("role", "CLIENTE")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7_200_000L))
                .setExpiration(new Date(System.currentTimeMillis() - 3_600_000L))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act / Assert
        assertThatThrownBy(() -> jwtService.extractClaims(expiredToken)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldRejectToken_whenItIsMalformed() {
        // Arrange
        String malformedToken = "esto-no-es-un-jwt";

        // Act / Assert
        assertThatThrownBy(() -> jwtService.extractClaims(malformedToken)).isInstanceOf(JwtException.class);
    }
}
