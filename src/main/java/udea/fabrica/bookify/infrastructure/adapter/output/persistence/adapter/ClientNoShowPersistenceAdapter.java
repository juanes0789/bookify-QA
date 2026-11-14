package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import udea.fabrica.bookify.domain.model.ClientNoShowSummary;
import udea.fabrica.bookify.domain.port.out.ClientNoShowOutputPort;

@Repository
public class ClientNoShowPersistenceAdapter implements ClientNoShowOutputPort {
    private static final String CLIENTS_QUERY = """
            SELECT u.id_usuario AS cliente_id,
                   TRIM(CONCAT(u.nombres, ' ', COALESCE(u.apellidos, ''))) AS nombre,
                   u.correo AS correo,
                   u.telefono AS telefono,
                   COUNT(r.id_reserva) AS inasistencias
            FROM tbl_reserva r
            INNER JOIN tbl_usuario u ON u.id_usuario = r.id_cliente
            INNER JOIN tbl_servicio s ON s.id_servicio = r.id_servicio
            INNER JOIN tbl_disponibilidad d ON d.id_disponibilidad = r.id_disponibilidad
            INNER JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
            WHERE s.id_proveedor = ?
              AND er.codigo = 'NO_ASISTIO'
              AND d.inicio_at >= ?
              AND d.inicio_at <= ?
            GROUP BY u.id_usuario, u.nombres, u.apellidos, u.correo, u.telefono
            ORDER BY inasistencias DESC, nombre ASC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public ClientNoShowPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ClientNoShowSummary> findClientsWithMostNoShows(UUID providerId,
            OffsetDateTime startDateTime, OffsetDateTime endDateTime, Integer limite) {
        return jdbcTemplate.query(CLIENTS_QUERY, (result, rowNumber) ->
                new ClientNoShowSummary(result.getObject("cliente_id", UUID.class),
                        result.getString("nombre"), result.getString("correo"),
                        result.getString("telefono"), result.getLong("inasistencias")),
                providerId, startDateTime, endDateTime, limite);
    }
}
