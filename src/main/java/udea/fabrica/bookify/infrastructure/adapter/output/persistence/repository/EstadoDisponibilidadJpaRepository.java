package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoDisponibilidadEntity;

import java.util.Optional;

public interface EstadoDisponibilidadJpaRepository extends JpaRepository<EstadoDisponibilidadEntity, Integer> {
    Optional<EstadoDisponibilidadEntity> findByCodigo(String codigo);
}