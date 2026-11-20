package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.out.ProviderAvailabilityPort;

@Repository
public class ProviderAvailabilityJdbcAdapter implements ProviderAvailabilityPort {

    private final JdbcTemplate jdbc;

    public ProviderAvailabilityJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean lockOwnedAvailability(UUID availabilityId, UUID providerId) {
        List<UUID> ids = jdbc.queryForList("""
                SELECT d.id_disponibilidad
                FROM tbl_disponibilidad d
                JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio
                JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
                JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
                WHERE d.id_disponibilidad = ? AND s.id_proveedor = ?
                FOR UPDATE OF d
                """, UUID.class, availabilityId, providerId);
        return !ids.isEmpty();
    }

    @Override
    public int countConfirmedBookings(UUID availabilityId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT r.id_reserva)
                FROM tbl_reserva r
                JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
                WHERE er.codigo = 'CONFIRMADA'
                  AND (r.id_disponibilidad = ?
                       OR EXISTS (SELECT 1 FROM tbl_ocupacion_disponibilidad od
                                  WHERE od.id_reserva = r.id_reserva AND od.id_disponibilidad = ?))
                """, Integer.class, availabilityId, availabilityId);
        return count == null ? 0 : count;
    }

    @Override
    public boolean existsOverlappingExcluding(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt) {
        Boolean exists = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM tbl_disponibilidad o
                    WHERE o.id_agenda_servicio = (SELECT id_agenda_servicio FROM tbl_disponibilidad
                                                  WHERE id_disponibilidad = ?)
                      AND o.id_disponibilidad <> ?
                      AND ? < o.fin_at AND ? > o.inicio_at)
                """, Boolean.class, availabilityId, availabilityId, inicioAt, finAt);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Availability updateRange(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt) {
        jdbc.update("""
                UPDATE tbl_disponibilidad
                SET inicio_at = ?, fin_at = ?, fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_disponibilidad = ?
                """, inicioAt, finAt, availabilityId);

        return jdbc.queryForObject("""
                SELECT id_disponibilidad, id_agenda_servicio, id_estado_disponibilidad, inicio_at, fin_at,
                       id_usuario_creador, fecha_creacion, fecha_actualizacion
                FROM tbl_disponibilidad WHERE id_disponibilidad = ?
                """,
                (rs, rowNum) -> new Availability(
                        rs.getObject("id_disponibilidad", UUID.class),
                        rs.getObject("id_agenda_servicio", UUID.class),
                        rs.getInt("id_estado_disponibilidad"),
                        rs.getObject("inicio_at", OffsetDateTime.class),
                        rs.getObject("fin_at", OffsetDateTime.class),
                        rs.getObject("id_usuario_creador", UUID.class),
                        rs.getObject("fecha_creacion", OffsetDateTime.class),
                        rs.getObject("fecha_actualizacion", OffsetDateTime.class)),
                availabilityId);
    }

    @Override
    public void delete(UUID availabilityId) {
        try {
            jdbc.update("DELETE FROM tbl_disponibilidad WHERE id_disponibilidad = ?", availabilityId);
        } catch (DataIntegrityViolationException ex) {
            throw new DomainConflictException(
                    "El horario tiene reservas históricas asociadas y no puede eliminarse");
        }
    }
}