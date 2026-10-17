package udea.fabrica.bookify.domain.port.out;

import java.util.Optional;
import udea.fabrica.bookify.domain.model.AuthenticatedUser;

/** The credential implementation is replaceable (local database, OIDC, etc.). */
public interface LoginAuthenticationPort {
    /**
     * Verifies credentials and returns the JWT claims source from the same lookup.
     */
    Optional<AuthenticatedUser> authenticate(String normalizedEmail, String password);
}
