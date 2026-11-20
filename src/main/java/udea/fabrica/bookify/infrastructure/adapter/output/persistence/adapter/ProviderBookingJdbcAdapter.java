package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import udea.fabrica.bookify.domain.model.ProviderBooking;
import udea.fabrica.bookify.domain.port.out.ProviderBookingQueryPort;

@Repository
public class ProviderBookingJdbcAdapter implements ProviderBookingQueryPort {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");

    private final JdbcTemplate jdbc;

    public ProviderBookingJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ProviderBooking> findByProviderAndDateRange(UUID providerId, LocalDate from, LocalDate to) {
        return jdbc.query("""
                SELECT d.inicio_at,
                       TRIM(CONCAT(u.nombres, ' ', u.apellidos)) AS cliente,
                       s.nombre AS servicio,
                       rec.nombre AS recurso,
                       er.codigo AS estado
                FROM tbl_reserva r
                JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
                JOIN tbl_disponibilidad d ON d.id_disponibilidad = r.id_disponibilidad
                JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio
                JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
                JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
                JOIN tbl_usuario u ON u.id_usuario = r.id_cliente
                LEFT JOIN tbl_reserva_recurso rr ON rr.id_reserva = r.id_reserva
                LEFT JOIN tbl_recurso rec ON rec.id_recurso = COALESCE(rr.id_recurso, d.id_recurso)
                WHERE s.id_proveedor = ?
                  AND CAST(d.inicio_at AT TIME ZONE 'America/Bogota' AS date) BETWEEN ? AND ?
                ORDER BY d.inicio_at DESC
                """,
                (rs, rowNum) -> {
                    OffsetDateTime inicio = rs.getObject("inicio_at", OffsetDateTime.class);
                    ZonedDateTime local = inicio.atZoneSameInstant(BOGOTA_ZONE);
                    return new ProviderBooking(
                            local.toLocalDate(),
                            local.toLocalTime(),
                            rs.getString("cliente"),
                            rs.getString("servicio"),
                            rs.getString("recurso"),
                            rs.getString("estado"));
                },
                providerId, Date.valueOf(from), Date.valueOf(to));
    }
}