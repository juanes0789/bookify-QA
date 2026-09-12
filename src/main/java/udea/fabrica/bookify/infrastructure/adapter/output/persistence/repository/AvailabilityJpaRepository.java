package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

import java.time.OffsetDateTime;
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
}