package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.NonWorkingDayEntity;

import java.time.LocalDate;
import java.util.UUID;

public interface NonWorkingDayJpaRepository extends JpaRepository<NonWorkingDayEntity, UUID> {

    boolean existsByProveedorIdAndFecha(UUID proveedorId, LocalDate fecha);

    @Query(value = """
            SELECT s.id_proveedor
            FROM tbl_agenda_servicio ag
            INNER JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
            INNER JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
            WHERE ag.id_agenda_servicio = :agendaServicioId
            LIMIT 1
            """, nativeQuery = true)
    UUID findProviderIdByAgendaServicioId(@Param("agendaServicioId") UUID agendaServicioId);
}
