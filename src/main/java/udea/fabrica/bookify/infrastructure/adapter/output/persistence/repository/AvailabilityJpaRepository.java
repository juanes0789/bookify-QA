package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilityJpaRepository extends JpaRepository<AvailabilityEntity, UUID> {

    @Query("SELECT COUNT(a) > 0 FROM AvailabilityEntity a " +
           "WHERE a.agendaServicioId = :agendaServicioId " +
           "AND (:inicioAt < a.finAt AND :finAt > a.inicioAt)")
    boolean existsOverlappingAvailability(
            @Param("agendaServicioId") UUID agendaServicioId,
            @Param("inicioAt") OffsetDateTime inicioAt,
            @Param("finAt") OffsetDateTime finAt
    );

    @Query(value = """
            SELECT
                s.id AS serviceId,
                s.nombre AS service,
                p.nombre_comercial AS provider,
                d.id AS availabilityId,
                d.inicio_at AS inicioAt,
                d.fin_at AS finAt
            FROM tbl_servicio s
            INNER JOIN tbl_proveedor p ON p.id = s.proveedor_id
            INNER JOIN tbl_servicio_sede ss ON ss.servicio_id = s.id
            INNER JOIN tbl_agenda_servicio ag ON ag.servicio_sede_id = ss.id
            INNER JOIN tbl_disponibilidad d ON d.agenda_servicio_id = ag.id
            INNER JOIN tbl_estado_disponibilidad ed ON ed.id = d.estado_disponibilidad_id
            WHERE s.activo = true
              AND ag.activo = true
              AND UPPER(ed.codigo) = 'DISPONIBLE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM tbl_ocupacion_disponibilidad od
                  INNER JOIN tbl_reserva r ON r.id = od.reserva_id
                  INNER JOIN tbl_estado_reserva er ON er.id = r.estado_reserva_id
                  WHERE od.disponibilidad_id = d.id
                    AND UPPER(er.codigo) LIKE 'CONFIRMAD%'
              )
            ORDER BY s.nombre, p.nombre_comercial, d.inicio_at
            """, nativeQuery = true)
    List<ServiceCatalogProjection> findAvailableServiceCatalog();
}
