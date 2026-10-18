package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.LoginAccessControl;
import udea.fabrica.bookify.domain.port.out.LoginAccessControlPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ControlAccesoLoginEntity;

@Component
public class LoginAccessControlPersistenceAdapter implements LoginAccessControlPort {
    @PersistenceContext private EntityManager entityManager;
    @Override public Optional<LoginAccessControl> findByNormalizedEmail(String email) {
        return Optional.ofNullable(entityManager.find(ControlAccesoLoginEntity.class, email)).map(this::toDomain);
    }
    @Override public void save(LoginAccessControl control) {
        ControlAccesoLoginEntity entity = entityManager.find(ControlAccesoLoginEntity.class, control.normalizedEmail());
        OffsetDateTime now = OffsetDateTime.now();
        if (entity == null) { entity = new ControlAccesoLoginEntity(); entity.setNormalizedEmail(control.normalizedEmail()); entity.setCreatedAt(now); }
        entity.setFailedAttempts(control.failedAttempts()); entity.setBlockedUntil(control.blockedUntil());
        entity.setLastFailedAttemptAt(control.lastFailedAttemptAt()); entity.setUpdatedAt(now);
        // Una entidad administrada se sincroniza sola al confirmar la transacción; solo la nueva se persiste.
        if (!entityManager.contains(entity)) entityManager.persist(entity);
    }
    @Override public void clear(String email) { entityManager.createQuery("delete from ControlAccesoLoginEntity c where c.normalizedEmail = :email").setParameter("email", email).executeUpdate(); }
    private LoginAccessControl toDomain(ControlAccesoLoginEntity e) { return new LoginAccessControl(e.getNormalizedEmail(), e.getFailedAttempts(), e.getBlockedUntil(), e.getLastFailedAttemptAt()); }
}
