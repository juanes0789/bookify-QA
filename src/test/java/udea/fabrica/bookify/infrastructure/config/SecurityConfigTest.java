package udea.fabrica.bookify.infrastructure.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import udea.fabrica.bookify.domain.model.UserRole;

/**
 * Levanta solo SecurityConfig, JwtService y un controlador de prueba, sin base de datos,
 * para verificar el filtro JWT y las reglas por rol con la cadena de seguridad real.
 */
class SecurityConfigTest {
    private static final String PUBLIC_ENDPOINT = "/api/v1/auth/ping";
    private static final String PROTECTED_ENDPOINT = "/api/v1/privado";
    private static final String CATALOG_ENDPOINT = "/api/v1/catalog/services";
    private static final String SEARCH_ENDPOINT = "/api/v1/catalog/services/search";
    private static final String PROVIDER_ENDPOINT = "/api/v1/comercio/perfil";
    private static final String CLIENT_ENDPOINT = "/api/v1/reservas/mis";

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SecurityConfig.class, TestWebConfig.class);
        context.refresh();
        jwtService = context.getBean(JwtService.class);
        Filter securityFilterChain = context.getBean("springSecurityFilterChain", Filter.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void shouldAllowAuthEndpoint_whenNoTokenIsSent() throws Exception {
        // Arrange
        var request = get(PUBLIC_ENDPOINT);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("publico"));
    }

    @Test
    void shouldReturn401_whenProtectedEndpointIsCalledWithoutToken() throws Exception {
        // Arrange
        var request = get(PROTECTED_ENDPOINT);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenAuthorizationHeaderIsNotBearer() throws Exception {
        // Arrange
        var request = get(PROTECTED_ENDPOINT).header("Authorization", "Basic YW5hOmNsYXZl");

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateUserFromToken_whenValidBearerTokenIsSent() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "carlos@barberia.com", UserRole.PROVEEDOR);
        var request = get(PROTECTED_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(userId + "|PROVEEDOR"));
    }

    @Test
    void shouldNormalizeRole_whenTokenRoleHasPrefixAndLowercase() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = signedToken(userId.toString(), " role_cliente ", 3_600_000L);
        var request = get(PROTECTED_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(userId + "|CLIENTE"));
    }

    @Test
    void shouldReturn401_whenTokenHasNoRoleClaim() throws Exception {
        // Arrange
        String token = signedToken(UUID.randomUUID().toString(), null, 3_600_000L);
        var request = get(PROTECTED_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenTokenIsExpired() throws Exception {
        // Arrange
        String token = signedToken(UUID.randomUUID().toString(), "CLIENTE", -60_000L);
        var request = get(PROTECTED_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenTokenIsSignedWithAnotherKey() throws Exception {
        // Arrange
        JwtService foreignService = new JwtService();
        ReflectionTestUtils.setField(foreignService, "secretKey", "otraClaveDistintaParaFirmarTokensDeBookify2026");
        String token = foreignService.generateToken(UUID.randomUUID(), "ana@mail.com", UserRole.CLIENTE);
        var request = get(PROTECTED_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExposeBcryptEncoder_whenPasswordEncoderBeanIsCreated() {
        // Arrange
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

        // Act
        String hash = encoder.encode("MiContrasenaSegura2026!");

        // Assert
        assertThat(hash).startsWith("$2a$");
        assertThat(encoder.matches("MiContrasenaSegura2026!", hash)).isTrue();
    }

    @Test
    void shouldAllowCatalog_whenNoTokenIsSent() throws Exception {
        // Arrange
        var request = get(CATALOG_ENDPOINT);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("catalogo"));
    }

    @Test
    void shouldAllowSearch_whenNoTokenIsSent() throws Exception {
        // Arrange
        var request = get(SEARCH_ENDPOINT);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("busqueda"));
    }

    @Test
    void shouldReturn401_whenProviderEndpointIsCalledWithoutToken() throws Exception {
        // Arrange
        var request = get(PROVIDER_ENDPOINT);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403_whenClienteCallsProviderEndpoint() throws Exception {
        // Arrange
        String token = jwtService.generateToken(UUID.randomUUID(), "ana@mail.com", UserRole.CLIENTE);
        var request = get(PROVIDER_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowProviderEndpoint_whenRoleIsProveedor() throws Exception {
        // Arrange
        String token = jwtService.generateToken(UUID.randomUUID(), "carlos@barberia.com", UserRole.PROVEEDOR);
        var request = get(PROVIDER_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("proveedor"));
    }

    @Test
    void shouldReturn403_whenProveedorCallsClientEndpoint() throws Exception {
        // Arrange
        String token = jwtService.generateToken(UUID.randomUUID(), "carlos@barberia.com", UserRole.PROVEEDOR);
        var request = get(CLIENT_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request).andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowClientEndpoint_whenRoleIsCliente() throws Exception {
        // Arrange
        String token = jwtService.generateToken(UUID.randomUUID(), "ana@mail.com", UserRole.CLIENTE);
        var request = get(CLIENT_ENDPOINT).header("Authorization", "Bearer " + token);

        // Act / Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("cliente"));
    }

    private String signedToken(String subject, String role, long millisToExpire) {
        String secret = (String) ReflectionTestUtils.getField(jwtService, "secretKey");
        var builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000L))
                .setExpiration(new Date(System.currentTimeMillis() + millisToExpire));
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
    }

    @Configuration
    @EnableWebMvc
    static class TestWebConfig {
        @Bean
        JwtService jwtService() {
            return new JwtService();
        }

        @Bean
        ProbeController probeController() {
            return new ProbeController();
        }
    }

    @RestController
    static class ProbeController {
        @GetMapping(PUBLIC_ENDPOINT)
        String publicEndpoint() {
            return "publico";
        }

        @GetMapping(PROTECTED_ENDPOINT)
        String protectedEndpoint() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getName() + "|" + authentication.getAuthorities().iterator().next().getAuthority();
        }

        @GetMapping(CATALOG_ENDPOINT)
        String catalogEndpoint() {
            return "catalogo";
        }

        @GetMapping(SEARCH_ENDPOINT)
        String searchEndpoint() {
            return "busqueda";
        }

        @GetMapping(PROVIDER_ENDPOINT)
        String providerEndpoint() {
            return "proveedor";
        }

        @GetMapping(CLIENT_ENDPOINT)
        String clientEndpoint() {
            return "cliente";
        }
    }
}