package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.DiaNoLaborableEntity;

@Component
public class NonWorkingDayPersistenceAdapter implements NonWorkingDayPort, NonWorkingDayOutputPort {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");

    @PersistenceContext private EntityManager entityManager;
    @Override public List<UUID> findAffectedBookingIds(UUID providerId, LocalDate date) {
        @SuppressWarnings("unchecked")
        List<UUID> ids = entityManager.createNativeQuery("SELECT r.id_reserva FROM tbl_reserva r " +
                "JOIN tbl_servicio s ON s.id_servicio = r.id_servicio " +
                "WHERE s.id_proveedor = :providerId AND r.fecha_reserva >= :start AND r.fecha_reserva < :end")
                .setParameter("providerId", providerId)
                .setParameter("start", date.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime())
                .setParameter("end", date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toOffsetDateTime())
                .getResultList();
        return ids;
    }
    @Override
    @Transactional
    public NonWorkingDay save(NonWorkingDay day) {
        DiaNoLaborableEntity e = new DiaNoLaborableEntity();
        // IMPORTANTE: NO se setea el ID. Con @GeneratedValue, el ID debe quedar null
        // para que Hibernate lo reconozca como entidad nueva (transient) y genere el UUID.
        e.setProviderId(day.getProveedorId());
        e.setDate(day.getFecha());
        e.setReason(day.getMotivo());
        e.setCreatedByUserId(day.getCreatedByUserId());
        e.setCreatedAt(day.getFechaCreacion());
        entityManager.persist(e);
        // Hibernate asigna el UUID generado en e.getId() tras el persist
        day.setId(e.getId());
        return day;
    }
    @Override
    public boolean existsByProveedorIdAndFecha(UUID proveedorId, LocalDate fecha) {
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT count(*) FROM tbl_dia_no_laborable WHERE id_proveedor = :proveedorId AND fecha = :fecha")
                .setParameter("proveedorId", proveedorId)
                .setParameter("fecha", fecha)
                .getSingleResult();
        return count.intValue() > 0;
    }

    @Override
    public boolean isNonWorkingDay(UUID proveedorId, LocalDate fecha) {
        return existsByProveedorIdAndFecha(proveedorId, fecha);
    }

    @Override
    public UUID findProviderIdByAgendaServicioId(UUID agendaServicioId) {
        try {
            return (UUID) entityManager.createNativeQuery(
                    "SELECT s.id_proveedor FROM tbl_agenda_servicio ag " +
                    "JOIN tbl_servicio_sede ss ON ag.id_servicio_sede = ss.id_servicio_sede " +
                    "JOIN tbl_servicio s ON ss.id_servicio = s.id_servicio " +
                    "WHERE ag.id_agenda_servicio = :agendaId")
                    .setParameter("agendaId", agendaServicioId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
