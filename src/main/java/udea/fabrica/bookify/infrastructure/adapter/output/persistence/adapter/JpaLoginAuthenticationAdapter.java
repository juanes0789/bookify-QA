package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;

/**
 * Local credential adapter. Password verification belongs here, never in the
 * access-control table: unknown addresses must still be recorded by HU-08.
 * The deployment schema exposes the stored hash as contrasena_hash.
 */
@Component
public class JpaLoginAuthenticationAdapter implements LoginAuthenticationPort {
    @PersistenceContext private EntityManager entityManager;
    @Override public boolean credentialsMatch(String normalizedEmail, String password) {
        if (password == null) return false;
        Number matches = (Number) entityManager.createNativeQuery(
                "select count(*) from tbl_usuario where lower(correo) = :correo and contrasena_hash = :contrasena")
                .setParameter("correo", normalizedEmail).setParameter("contrasena", password).getSingleResult();
        return matches.longValue() == 1;
    }
}
