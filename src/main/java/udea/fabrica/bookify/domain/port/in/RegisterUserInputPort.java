package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.User;

public interface RegisterUserInputPort {
    User register(String nombre, String correo, String contrasena, String rol);
}
