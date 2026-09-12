package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AgendaServicioEntity;

import java.util.UUID;

public interface AgendaServicioJpaRepository extends JpaRepository<AgendaServicioEntity, UUID> {
    boolean existsByIdAndActivoTrue(UUID id);
}