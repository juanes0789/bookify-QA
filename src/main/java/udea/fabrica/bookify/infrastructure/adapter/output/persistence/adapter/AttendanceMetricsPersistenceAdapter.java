package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import udea.fabrica.bookify.domain.model.AttendancePeriodSummary;
import udea.fabrica.bookify.domain.port.out.AttendanceMetricsOutputPort;

@Repository
public class AttendanceMetricsPersistenceAdapter implements AttendanceMetricsOutputPort {
    private static final String SUMMARY_QUERY = """
            SELECT
                COUNT(CASE WHEN er.codigo = 'ATENDIDA' AND d.fin_at < ? THEN 1 END) AS atendidas,
                COUNT(CASE WHEN er.codigo = 'NO_ASISTIO' AND d.fin_at < ? THEN 1 END) AS no_asistidas,
                COUNT(CASE WHEN er.codigo = 'CANCELADA' THEN 1 END) AS canceladas,
                COUNT(CASE WHEN er.codigo = 'CONFIRMADA' AND d.fin_at < ? THEN 1 END) AS confirmadas_pasadas
            FROM tbl_reserva r
            INNER JOIN tbl_servicio s ON s.id_servicio = r.id_servicio
            INNER JOIN tbl_disponibilidad d ON d.id_disponibilidad = r.id_disponibilidad
            INNER JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
            WHERE s.id_proveedor = ?
              AND d.inicio_at >= ?
              AND d.inicio_at <= ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public AttendanceMetricsPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AttendancePeriodSummary getPeriodSummary(UUID providerId, OffsetDateTime startDateTime,
                                                    OffsetDateTime endDateTime, OffsetDateTime now) {
        return jdbcTemplate.queryForObject(SUMMARY_QUERY, (result, rowNumber) ->
                        new AttendancePeriodSummary(result.getLong("atendidas"),
                                result.getLong("no_asistidas"), result.getLong("canceladas"),
                                result.getLong("confirmadas_pasadas")),
                now, now, now, providerId, startDateTime, endDateTime);
    }
}
