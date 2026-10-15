package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UsuarioRolEntity;

public interface UsuarioRolJpaRepository extends JpaRepository<UsuarioRolEntity, UsuarioRolEntity.UsuarioRolId> {
}
