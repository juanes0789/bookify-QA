package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoUsuarioEntity;

import java.util.Optional;

public interface EstadoUsuarioJpaRepository extends JpaRepository<EstadoUsuarioEntity, Integer> {
    Optional<EstadoUsuarioEntity> findByCodigo(String codigo);
}
