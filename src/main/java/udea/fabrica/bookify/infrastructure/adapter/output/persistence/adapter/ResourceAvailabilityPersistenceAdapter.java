package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.port.out.ResourceAvailabilityPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ReservaRecursoEntity;

@Component
public class ResourceAvailabilityPersistenceAdapter implements ResourceAvailabilityPort {
    
    @PersistenceContext 
    private EntityManager entityManager;
    
    public ResourceAvailabilityPersistenceAdapter() { }
    
    public ResourceAvailabilityPersistenceAdapter(EntityManager entityManager) { 
        this.entityManager = entityManager; 
    }
    
    @Override 
    public Optional<UUID> findAvailableResource(UUID providerId, OffsetDateTime startAt, OffsetDateTime endAt) {
        return entityManager.createQuery(
                "SELECT r.id FROM RecursoEntity r WHERE r.proveedorId = :idProveedor AND r.id NOT IN " +
                "(SELECT rr.recursoId FROM ReservaRecursoEntity rr " +
                "JOIN rr.reserva res " +
                "JOIN res.disponibilidad disp " + // <--- AQUÍ USAMOS EL NOMBRE EXACTO DE LA VARIABLE
                "WHERE res.estado.codigo = 'CONFIRMADA' " +
                "AND disp.inicioAt < :finAt AND disp.finAt > :inicioAt)", UUID.class)
                .setParameter("idProveedor", providerId)
                .setParameter("inicioAt", startAt)
                .setParameter("finAt", endAt)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
    
    @Override 
    public void assignResourceToBooking(UUID bookingId, UUID resourceId) {
        // 1. Verificamos si la reserva ya tiene un recurso asignado para evitar el doble INSERT
        Long asignacionesPrevias = entityManager.createQuery(
                "SELECT count(rr) FROM ReservaRecursoEntity rr WHERE rr.reservaId = :reservaId", Long.class)
                .setParameter("reservaId", bookingId)
                .getSingleResult();
        
        // 2. Solo insertamos si es la primera vez que se llama a este método para esta reserva
        if (asignacionesPrevias == 0) {
            ReservaRecursoEntity relation = new ReservaRecursoEntity(); 
            relation.setReservaId(bookingId); 
            relation.setRecursoId(resourceId); 
            entityManager.persist(relation);
        }
    }
}