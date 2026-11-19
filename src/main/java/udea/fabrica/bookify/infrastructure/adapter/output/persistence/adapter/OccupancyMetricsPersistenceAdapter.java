package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import udea.fabrica.bookify.domain.model.OccupancySlot;
import udea.fabrica.bookify.domain.model.OccupancySourceData;
import udea.fabrica.bookify.domain.port.out.OccupancyMetricsOutputPort;

@Repository
public class OccupancyMetricsPersistenceAdapter implements OccupancyMetricsOutputPort {
    private static final String AGENDA = """
            FROM tbl_disponibilidad d
            JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio
            JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
            JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
            JOIN tbl_estado_disponibilidad ed ON ed.id_estado_disponibilidad = d.id_estado_disponibilidad
            WHERE s.id_proveedor = ? AND d.inicio_at < ? AND d.fin_at > ?
            """;
    private final JdbcTemplate jdbc;
    public OccupancyMetricsPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public OccupancySourceData getSourceData(UUID providerId, OffsetDateTime start, OffsetDateTime end) {
        List<OccupancySlot> available = slots("SELECT d.inicio_at, d.fin_at " + AGENDA
                + " AND UPPER(ed.codigo) IN ('DISPONIBLE', 'OCUPADA')", providerId, end, start);
        List<OccupancySlot> blocked = slots("SELECT d.inicio_at, d.fin_at " + AGENDA
                + " AND UPPER(ed.codigo) NOT IN ('DISPONIBLE', 'OCUPADA')", providerId, end, start);
        List<OccupancySlot> booked = slots("""
                SELECT d.inicio_at, d.fin_at FROM tbl_reserva r
                JOIN tbl_ocupacion_disponibilidad od ON od.id_reserva = r.id_reserva
                JOIN tbl_disponibilidad d ON d.id_disponibilidad = od.id_disponibilidad
                JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio
                JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
                JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
                JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
                WHERE s.id_proveedor = ? AND d.inicio_at < ? AND d.fin_at > ?
                  AND UPPER(er.codigo) LIKE 'CONFIRMAD%'
                """, providerId, end, start);
        return new OccupancySourceData(available, blocked, booked);
    }
    private List<OccupancySlot> slots(String sql, UUID providerId, OffsetDateTime end, OffsetDateTime start) {
        return jdbc.query(sql, (ResultSet rs, int row) -> new OccupancySlot(
                rs.getObject("inicio_at", OffsetDateTime.class), rs.getObject("fin_at", OffsetDateTime.class)),
                providerId, end, start);
    }
}
