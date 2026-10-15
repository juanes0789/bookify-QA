package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.RolEntity;

import java.util.Optional;

public interface RolJpaRepository extends JpaRepository<RolEntity, Integer> {
    Optional<RolEntity> findByCodigo(String codigo);
}
