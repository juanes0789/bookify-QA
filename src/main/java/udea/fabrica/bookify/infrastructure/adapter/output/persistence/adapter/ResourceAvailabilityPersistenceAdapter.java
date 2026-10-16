package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.port.out.ResourceAvailabilityPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ReservaRecursoEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ReservaRecursoId;

@Component
public class ResourceAvailabilityPersistenceAdapter implements ResourceAvailabilityPort {
    @PersistenceContext private EntityManager entityManager;
    public ResourceAvailabilityPersistenceAdapter() { }
    public ResourceAvailabilityPersistenceAdapter(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public Optional<UUID> findAvailableResource(UUID providerId, OffsetDateTime startAt, OffsetDateTime endAt) {
        return entityManager.createQuery("SELECT r.id FROM RecursoEntity r WHERE r.proveedorId = :idProveedor AND r.id NOT IN " +
                "(SELECT rr.recursoId FROM ReservaRecursoEntity rr JOIN rr.reserva res WHERE res.estado.codigo = 'CONFIRMADA' " +
                "AND res.inicioAt < :finAt AND res.finAt > :inicioAt)", UUID.class)
                .setParameter("idProveedor", providerId).setParameter("inicioAt", startAt).setParameter("finAt", endAt)
                .setMaxResults(1).getResultStream().findFirst();
    }
    @Override public void assignResourceToBooking(UUID bookingId, UUID resourceId) {
        ReservaRecursoEntity relation = new ReservaRecursoEntity(); relation.setReservaId(bookingId); relation.setRecursoId(resourceId); entityManager.persist(relation);
    }
}
