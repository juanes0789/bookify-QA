package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
}