package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.exception.EmailAlreadyExistsException;
import udea.fabrica.bookify.domain.exception.InvalidPasswordException;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.out.CommonPasswordPort;
import udea.fabrica.bookify.domain.port.out.PasswordEncoderPort;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {

    private static final String VALID_PASSWORD = "MiContrasenaSegura2026!";
    private static final String VALID_EMAIL = "ana@mail.com";
    private static final String VALID_NAME = "Ana Gómez";

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoderPort passwordEncoderPort;
    private CommonPasswordPort commonPasswordPort;
    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        userRepositoryPort = new FakeUserRepository();
        passwordEncoderPort = raw -> "hashed-" + raw;
        commonPasswordPort = password -> "contraseña123456".equalsIgnoreCase(password);
        service = new RegisterUserService(userRepositoryPort, passwordEncoderPort, commonPasswordPort);
    }

    // ─── Escenarios de éxito ────────────────────────────────────────────────────

    @Test
    void shouldRegisterClientSuccessfully() {
        User user = service.register(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, "CLIENTE");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getNombreCompleto()).isEqualTo("Ana Gómez");
        assertThat(user.getCorreo()).isEqualTo(VALID_EMAIL);
        assertThat(user.getRol()).isEqualTo(UserRole.CLIENTE);
        assertThat(user.isActivo()).isTrue();
        assertThat(user.getPasswordHash()).isNull(); // El hash nunca sale del dominio
    }

    @Test
    void shouldRegisterProviderSuccessfully() {
        User user = service.register("Carlos Barbería", "carlos@barberia.com", VALID_PASSWORD, "PROVEEDOR");

        assertThat(user.getRol()).isEqualTo(UserRole.PROVEEDOR);
    }

    @Test
    void shouldNormalizeEmailToLowercase() {
        User user = service.register(VALID_NAME, "ANA@MAIL.COM", VALID_PASSWORD, "CLIENTE");

        assertThat(user.getCorreo()).isEqualTo("ana@mail.com");
    }

    @Test
    void shouldSplitNombreIntoNombresAndApellidos() {
        User user = service.register("Ana Gómez", VALID_EMAIL, VALID_PASSWORD, "CLIENTE");

        assertThat(user.getNombres()).isEqualTo("Ana");
        assertThat(user.getApellidos()).isEqualTo("Gómez");
    }

    @Test
    void shouldRejectSingleWordName() {
        assertThatThrownBy(() -> service.register("Ana", VALID_EMAIL, VALID_PASSWORD, "CLIENTE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe proporcionar al menos un nombre y un apellido");
    }

    @Test
    void shouldCallPasswordEncoderPort() {
        boolean[] encoderCalled = {false};
        PasswordEncoderPort spyEncoder = raw -> {
            encoderCalled[0] = true;
            return "hashed";
        };
        service = new RegisterUserService(userRepositoryPort, spyEncoder, commonPasswordPort);

        service.register(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, "CLIENTE");

        assertThat(encoderCalled[0]).isTrue();
    }

    @Test
    void shouldAcceptPasswordOfExactly15Characters() {
        service.register(VALID_NAME, VALID_EMAIL, "a".repeat(15), "CLIENTE");
    }

    @Test
    void shouldAcceptPasswordOfExactly64Characters() {
        service.register(VALID_NAME, VALID_EMAIL, "a".repeat(64), "CLIENTE");
    }

    // ─── Validaciones de contraseña ─────────────────────────────────────────────

    @Test
    void shouldRejectPasswordShorterThan15Characters() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "corta123", "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("debe tener entre 15 y 64 caracteres");
    }

    @Test
    void shouldRejectPasswordLongerThan64Characters() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "a".repeat(65), "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("debe tener entre 15 y 64 caracteres");
    }

    @Test
    void shouldRejectPasswordContainingEmail() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "ana@mail.com-2026", "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("no puede contener tu correo");
    }

    @Test
    void shouldRejectPasswordContainingEmailInUpperCase() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "ANA@MAIL.COM-2026", "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("no puede contener tu correo");
    }

    @Test
    void shouldRejectPasswordContainingBookify() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "bookify-secret-pass", "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("no puede contener la palabra 'bookify'");
    }

    @Test
    void shouldRejectPasswordContainingBookifyInUpperCase() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "BOOKIFY-secret-pass", "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("no puede contener la palabra 'bookify'");
    }

    @Test
    void shouldRejectCommonPassword() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, "contraseña123456", "CLIENTE"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("es una contraseña común o comprometida");
    }

    // ─── Validaciones de correo ──────────────────────────────────────────────────

    @Test
    void shouldRejectDuplicateEmailCaseInsensitive() {
        service.register(VALID_NAME, "ana@mail.com", VALID_PASSWORD, "CLIENTE");

        assertThatThrownBy(() -> service.register("Otra Ana", "ANA@mail.com", VALID_PASSWORD, "CLIENTE"))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("El correo ya está registrado");
    }

    // ─── Validación de rol ───────────────────────────────────────────────────────

    @Test
    void shouldRejectInvalidRole() {
        assertThatThrownBy(() -> service.register(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rol no válido");
    }

    // ─── Fake en memoria ─────────────────────────────────────────────────────────

    static class FakeUserRepository implements UserRepositoryPort {
        private final java.util.Set<String> emails = new java.util.HashSet<>();

        @Override
        public boolean existsByEmail(String email) {
            return emails.contains(email.toLowerCase());
        }

        @Override
        public User save(User user) {
            emails.add(user.getCorreo().toLowerCase());
            return new User(
                    UUID.randomUUID(),
                    user.getNombres(),
                    user.getApellidos(),
                    user.getCorreo(),
                    null,
                    user.getRol(),
                    true,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return emails.contains(email.toLowerCase()) ? Optional.of(new User(
                    UUID.randomUUID(),
                    "Nombre",
                    "Apellido",
                    email,
                    null,
                    UserRole.CLIENTE,
                    true,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            )) : Optional.empty();
        }
    }
}
