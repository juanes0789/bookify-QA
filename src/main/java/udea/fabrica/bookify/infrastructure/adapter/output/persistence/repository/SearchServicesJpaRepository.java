package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

import java.util.List;
import java.util.UUID;

public interface SearchServicesJpaRepository extends Repository<AvailabilityEntity, UUID> {

    @Query(value = """
            SELECT
                s.id_servicio AS serviceId,
                s.nombre AS service,
                p.nombre_comercial AS provider,
                tp.nombre AS category,
                d.id_disponibilidad AS availabilityId,
                d.inicio_at AS inicioAt,
                d.fin_at AS finAt,
                r.nombre AS resourceName
            FROM tbl_servicio s
            INNER JOIN tbl_proveedor p ON p.id_proveedor = s.id_proveedor
            INNER JOIN tbl_tipo_proveedor tp ON tp.id_tipo_proveedor = p.id_tipo_proveedor
            INNER JOIN tbl_servicio_sede ss ON ss.id_servicio = s.id_servicio
            INNER JOIN tbl_agenda_servicio ag ON ag.id_servicio_sede = ss.id_servicio_sede
            INNER JOIN tbl_agenda a ON a.id_agenda = ag.id_agenda
            INNER JOIN tbl_agenda_recurso ar ON ar.id_agenda = a.id_agenda
            INNER JOIN tbl_recurso r ON r.id_recurso = ar.id_recurso
            INNER JOIN tbl_disponibilidad d ON d.id_agenda_servicio = ag.id_agenda_servicio
            INNER JOIN tbl_estado_disponibilidad ed ON ed.id_estado_disponibilidad = d.id_estado_disponibilidad
            WHERE s.activo = true
              AND p.activo = true
              AND ag.activo = true
              AND UPPER(ed.codigo) = 'DISPONIBLE'
              AND (
                  :texto IS NULL
                  OR unaccent(lower(s.nombre)) LIKE concat('%', unaccent(lower(:texto)), '%')
                  OR unaccent(lower(p.nombre_comercial)) LIKE concat('%', unaccent(lower(:texto)), '%')
              )
              AND (
                  :categoria IS NULL
                  OR unaccent(lower(tp.nombre)) = unaccent(lower(:categoria))
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM tbl_ocupacion_disponibilidad od
                  INNER JOIN tbl_reserva res ON res.id_reserva = od.id_reserva
                  INNER JOIN tbl_estado_reserva er ON er.id_estado_reserva = res.id_estado_reserva
                  WHERE od.id_disponibilidad = d.id_disponibilidad
                    AND UPPER(er.codigo) LIKE 'CONFIRMAD%'
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM tbl_dia_no_laborable dnl
                  WHERE dnl.id_proveedor = p.id_proveedor
                    AND dnl.fecha = CAST(d.inicio_at AT TIME ZONE 'America/Bogota' AS date)
              )
            ORDER BY s.nombre, p.nombre_comercial, d.inicio_at
            """, nativeQuery = true)
    List<SearchServicesProjection> findAvailableServices(
            @Param("texto") String texto,
            @Param("categoria") String categoria
    );
}
