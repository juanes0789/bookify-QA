package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

import java.util.List;
import java.util.UUID;

public interface BookingHistoryJpaRepository extends Repository<AvailabilityEntity, UUID> {

    @Query(value = """
            SELECT
                s.nombre AS service,
                p.nombre_comercial AS provider,
                d.inicio_at AS fechaHora,
                er.codigo AS estado
            FROM tbl_reserva r
            INNER JOIN tbl_estado_reserva er ON er.id = r.estado_reserva_id
            INNER JOIN tbl_ocupacion_disponibilidad od ON od.reserva_id = r.id
            INNER JOIN tbl_disponibilidad d ON d.id = od.disponibilidad_id
            INNER JOIN tbl_agenda_servicio ag ON ag.id = d.agenda_servicio_id
            INNER JOIN tbl_servicio_sede ss ON ss.id = ag.servicio_sede_id
            INNER JOIN tbl_servicio s ON s.id = ss.servicio_id
            INNER JOIN tbl_proveedor p ON p.id = s.proveedor_id
            WHERE r.cliente_id = :clienteId
            ORDER BY d.inicio_at DESC
            """, nativeQuery = true)
    List<BookingHistoryProjection> findBookingHistoryByClientId(@Param("clienteId") UUID clienteId);
}
