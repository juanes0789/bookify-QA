package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.AuthenticatedUser;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;

/**
 * Local credential adapter. Password verification belongs here, never in the
 * access-control table: unknown addresses must still be recorded by HU-08.
 * The deployment schema exposes the stored hash as password_hash.
 */
@Component
public class JpaLoginAuthenticationAdapter implements LoginAuthenticationPort {
    @PersistenceContext private EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    public JpaLoginAuthenticationAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<AuthenticatedUser> authenticate(String normalizedEmail, String password) {
        if (password == null) return Optional.empty();

        @SuppressWarnings("unchecked")
        var rows = entityManager.createNativeQuery("""
                select u.id_usuario, u.correo, u.password_hash, r.codigo
                from tbl_usuario u
                join tbl_usuario_rol ur on ur.id_usuario = u.id_usuario
                join tbl_rol r on r.id_rol = ur.id_rol
                where lower(u.correo) = :correo
                """)
                .setParameter("correo", normalizedEmail)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty()) return Optional.empty();
        Object[] row = (Object[]) rows.get(0);
        if (!passwordEncoder.matches(password, (String) row[2])) return Optional.empty();

        return Optional.of(new AuthenticatedUser(
                (UUID) row[0], (String) row[1], UserRole.valueOf((String) row[3])));
    }
}
