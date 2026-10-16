package udea.fabrica.bookify.domain.port.out;

import java.util.Optional;
import udea.fabrica.bookify.domain.model.LoginAccessControl;

public interface LoginAccessControlPort {
    Optional<LoginAccessControl> findByNormalizedEmail(String normalizedEmail);
    void save(LoginAccessControl control);
    void clear(String normalizedEmail);
}
