package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.DiaNoLaborableEntity;

@Component
public class NonWorkingDayPersistenceAdapter implements NonWorkingDayPort {
    @PersistenceContext private EntityManager entityManager;
    public NonWorkingDayPersistenceAdapter() { }
    public NonWorkingDayPersistenceAdapter(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public List<UUID> findAffectedBookingIds(UUID providerId, LocalDate date) {
        @SuppressWarnings("unchecked")
        List<UUID> ids = entityManager.createNativeQuery("SELECT r.id_reserva FROM tbl_reserva r " +
                "JOIN tbl_servicio s ON s.id_servicio = r.id_servicio " +
                "WHERE s.id_proveedor = :providerId AND r.fecha_reserva >= :start AND r.fecha_reserva < :end")
                .setParameter("providerId", providerId).setParameter("start", date.atStartOfDay().atOffset(java.time.ZoneOffset.UTC))
                .setParameter("end", date.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC)).getResultList();
        return ids;
    }
    @Override public NonWorkingDay save(NonWorkingDay day) {
        DiaNoLaborableEntity e = new DiaNoLaborableEntity(); e.setId(day.id()); e.setProviderId(day.providerId()); e.setDate(day.date());
        e.setReason(day.reason()); e.setCreatedByUserId(day.createdByUserId()); e.setCreatedAt(day.createdAt()); entityManager.persist(e); return day;
    }
}
