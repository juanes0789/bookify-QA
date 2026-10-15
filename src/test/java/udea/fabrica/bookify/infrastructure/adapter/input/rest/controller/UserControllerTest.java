package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import udea.fabrica.bookify.domain.exception.EmailAlreadyExistsException;
import udea.fabrica.bookify.domain.exception.InvalidPasswordException;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.in.RegisterUserInputPort;
import udea.fabrica.bookify.infrastructure.config.GlobalExceptionHandler;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    private TestJsonWriter objectMapper;

    private RegisterUserInputPort registerUserInputPort;

    private static final String ENDPOINT = "/api/v1/auth/register";
    private static final String VALID_PASSWORD = "MiContrasenaSegura2026!";

    @BeforeEach
    void setUp() {
        registerUserInputPort = mock(RegisterUserInputPort.class);
        objectMapper = new TestJsonWriter();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(registerUserInputPort))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─── Escenario: Registro exitoso de cliente ──────────────────────────────────

    @Test
    void shouldReturn201WhenRegisteringClient() throws Exception {
        UUID userId = UUID.randomUUID();
        User savedUser = new User(userId, "Ana", "Gómez", "ana@mail.com",
                null, UserRole.CLIENTE, true, OffsetDateTime.now(), OffsetDateTime.now());

        when(registerUserInputPort.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(savedUser);

        Map<String, String> body = Map.of(
                "nombre", "Ana Gómez",
                "correo", "ana@mail.com",
                "contrasena", VALID_PASSWORD,
                "rol", "CLIENTE"
        );

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nombre").value("Ana Gómez"))
                .andExpect(jsonPath("$.correo").value("ana@mail.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // ─── Escenario: Registro exitoso de proveedor ────────────────────────────────

    @Test
    void shouldReturn201WhenRegisteringProvider() throws Exception {
        UUID userId = UUID.randomUUID();
        User savedUser = new User(userId, "Carlos", "Barbería", "carlos@barberia.com",
                null, UserRole.PROVEEDOR, true, OffsetDateTime.now(), OffsetDateTime.now());

        when(registerUserInputPort.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(savedUser);

        Map<String, String> body = Map.of(
                "nombre", "Carlos Barbería",
                "correo", "carlos@barberia.com",
                "contrasena", VALID_PASSWORD,
                "rol", "PROVEEDOR"
        );

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PROVEEDOR"));
    }

    // ─── Escenario: Correo ya registrado ─────────────────────────────────────────

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(registerUserInputPort.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EmailAlreadyExistsException("El correo ya está registrado"));

        Map<String, String> body = Map.of(
                "nombre", "Ana Gómez",
                "correo", "ANA@mail.com",
                "contrasena", VALID_PASSWORD,
                "rol", "CLIENTE"
        );

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya está registrado"));
    }

    // ─── Escenario: Contraseña corta ──────────────────────────────────────────────

    @Test
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        when(registerUserInputPort.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InvalidPasswordException("debe tener entre 15 y 64 caracteres"));

        Map<String, String> body = Map.of(
                "nombre", "Ana Gómez",
                "correo", "ana@mail.com",
                "contrasena", "corta123",
                "rol", "CLIENTE"
        );

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.motivo").value("debe tener entre 15 y 64 caracteres"));
    }

    // ─── Escenario: Contraseña común ─────────────────────────────────────────────

    @Test
    void shouldReturn400WhenPasswordIsCommon() throws Exception {
        when(registerUserInputPort.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InvalidPasswordException("es una contraseña común o comprometida"));

        Map<String, String> body = Map.of(
                "nombre", "Ana Gómez",
                "correo", "ana@mail.com",
                "contrasena", "contraseña123456",
                "rol", "CLIENTE"
        );

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.motivo").value("es una contraseña común o comprometida"));
    }

    // ─── Escenario: Contraseña contiene correo ────────────────────────────────────

    @Test
    void shouldReturn400WhenPasswordContainsEmail() throws Exception {
        when(registerUserInputPort.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InvalidPasswordException("no puede contener tu correo"));

        Map<String, String> body = Map.of(
                "nombre", "Ana Gómez",
                "correo", "ana@mail.com",
                "contrasena", "ana@mail.com-2026",
                "rol", "CLIENTE"
        );

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.motivo").value("no puede contener tu correo"));
    }

    // ─── Validación Bean Validation: campos requeridos ───────────────────────────

    @Test
    void shouldReturn400WhenRequestBodyIsMissingRequiredFields() throws Exception {
        Map<String, String> body = Map.of("nombre", "Ana Gómez");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    private static class TestJsonWriter {
        String writeValueAsString(Map<String, String> body) {
            StringBuilder json = new StringBuilder("{");
            body.forEach((key, value) -> {
                if (json.length() > 1) {
                    json.append(',');
                }
                json.append('"').append(escape(key)).append('"')
                        .append(':')
                        .append('"').append(escape(value)).append('"');
            });
            return json.append('}').toString();
        }

        private static String escape(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
