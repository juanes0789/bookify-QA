package udea.fabrica.bookify.domain.port.out;

/** The credential implementation is replaceable (local database, OIDC, etc.). */
public interface LoginAuthenticationPort {
    boolean credentialsMatch(String normalizedEmail, String password);
}
