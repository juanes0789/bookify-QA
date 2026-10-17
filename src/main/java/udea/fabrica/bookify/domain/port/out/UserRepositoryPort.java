package udea.fabrica.bookify.domain.port.out;
import java.util.Optional;
import udea.fabrica.bookify.domain.model.User;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findByEmail(String email);
}
