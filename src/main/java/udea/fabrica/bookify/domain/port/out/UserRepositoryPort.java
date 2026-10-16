package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.User;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);
    User save(User user);
}
