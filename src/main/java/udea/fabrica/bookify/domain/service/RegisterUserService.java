package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.exception.EmailAlreadyExistsException;
import udea.fabrica.bookify.domain.exception.InvalidPasswordException;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.in.RegisterUserInputPort;
import udea.fabrica.bookify.domain.port.out.CommonPasswordPort;
import udea.fabrica.bookify.domain.port.out.PasswordEncoderPort;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;

import java.time.OffsetDateTime;

public class RegisterUserService implements RegisterUserInputPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final CommonPasswordPort commonPasswordPort;

    public RegisterUserService(UserRepositoryPort userRepositoryPort,
                               PasswordEncoderPort passwordEncoderPort,
                               CommonPasswordPort commonPasswordPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.commonPasswordPort = commonPasswordPort;
    }

    @Override
    public User register(String nombre, String correo, String contrasena, String rol) {

        // Paso 5.1: Validar rol
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Rol no válido");
        }

        // Paso 5.2: Validar longitud de contraseña (15–64 caracteres)
        if (contrasena == null || contrasena.length() < 15 || contrasena.length() > 64) {
            throw new InvalidPasswordException("debe tener entre 15 y 64 caracteres");
        }

        // Normalizar correo para las validaciones de contraseña
        String emailNormalizado = correo.trim().toLowerCase();

        // Paso 5.3: La contraseña no debe contener el correo
        if (contrasena.toLowerCase().contains(emailNormalizado)) {
            throw new InvalidPasswordException("no puede contener tu correo");
        }

        // Paso 5.4: La contraseña no debe contener "bookify"
        if (contrasena.toLowerCase().contains("bookify")) {
            throw new InvalidPasswordException("no puede contener la palabra 'bookify'");
        }

        // Paso 5.5: Validar contra lista negra de contraseñas comunes
        if (commonPasswordPort.isCommon(contrasena)) {
            throw new InvalidPasswordException("es una contraseña común o comprometida");
        }

        // Paso 5.7: Validar unicidad del correo (case-insensitive)
        if (userRepositoryPort.existsByEmail(emailNormalizado)) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        // Paso 5.8: Separar nombres y apellidos
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        if (!nombreNormalizado.contains(" ")) {
            throw new IllegalArgumentException("Debe proporcionar al menos un nombre y un apellido");
        }
        String[] partes = nombreNormalizado.split("\\s+", 2);
        String nombres = partes[0];
        String apellidos = partes[1];

        // Paso 5.9: Generar hash BCrypt
        String passwordHash = passwordEncoderPort.encode(contrasena);

        // Paso 5.10: Construir y guardar el usuario
        OffsetDateTime ahora = OffsetDateTime.now();
        User user = new User(null, nombres, apellidos, emailNormalizado,
                passwordHash, userRole, true, ahora, ahora);

        return userRepositoryPort.save(user);
    }
}
