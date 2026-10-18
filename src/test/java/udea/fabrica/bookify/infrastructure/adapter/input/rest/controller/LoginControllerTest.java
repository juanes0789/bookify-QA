package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import udea.fabrica.bookify.domain.model.AuthenticatedUser;
import udea.fabrica.bookify.domain.model.LoginResult;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.in.LoginUseCase;
import udea.fabrica.bookify.infrastructure.config.GlobalExceptionHandler;
import udea.fabrica.bookify.infrastructure.config.JwtService;

class LoginControllerTest {
    private static final String ENDPOINT = "/api/v1/auth/login";
    private static final String PASSWORD = "MiContrasenaSegura2026!";

    private LoginUseCase loginUseCase;
    private JwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        loginUseCase = mock(LoginUseCase.class);
        jwtService = mock(JwtService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LoginController(loginUseCase, jwtService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnBearerTokenWithRole_whenCredentialsAreValid() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "carlos@barberia.com", UserRole.PROVEEDOR);
        when(loginUseCase.login("carlos@barberia.com", PASSWORD)).thenReturn(LoginResult.authenticated(user));
        when(jwtService.generateToken(userId, "carlos@barberia.com", UserRole.PROVEEDOR)).thenReturn("token-firmado");

        // Act / Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("carlos@barberia.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-firmado"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.role").value("PROVEEDOR"));
    }

    @Test
    void shouldReturn401WithoutToken_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        when(loginUseCase.login("ana@mail.com", "ContrasenaIncorrecta2026"))
                .thenReturn(LoginResult.rejected(false, null));

        // Act / Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ana@mail.com", "ContrasenaIncorrecta2026")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void shouldReturn429AndRetryAfter_whenAccountIsBlocked() throws Exception {
        when(loginUseCase.login("ana@mail.com", "ContrasenaIncorrecta2026"))
                .thenReturn(LoginResult.rejected(true, OffsetDateTime.now().plusSeconds(60)));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ana@mail.com", "ContrasenaIncorrecta2026")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void shouldReturn400_whenEmailHasInvalidFormat() throws Exception {
        // Arrange
        String invalidBody = body("correo-sin-arroba", PASSWORD);

        // Act / Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(loginUseCase, jwtService);
    }

    @Test
    void shouldReturn400_whenPasswordIsBlank() throws Exception {
        // Arrange
        String invalidBody = body("ana@mail.com", " ");

        // Act / Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(loginUseCase, jwtService);
    }

    private static String body(String correo, String password) {
        return """
                {"correo":"%s","password":"%s"}
                """.formatted(correo, password);
    }
}
