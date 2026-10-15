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
            INNER JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
            INNER JOIN tbl_ocupacion_disponibilidad od ON od.id_reserva = r.id_reserva
            INNER JOIN tbl_disponibilidad d ON d.id_disponibilidad = od.id_disponibilidad
            INNER JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio
            INNER JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
            INNER JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
            INNER JOIN tbl_proveedor p ON p.id_proveedor = s.id_proveedor
            WHERE r.id_cliente = :clienteId
            ORDER BY d.inicio_at DESC
            """, nativeQuery = true)
    List<BookingHistoryProjection> findBookingHistoryByClientId(@Param("clienteId") UUID clienteId);
}
