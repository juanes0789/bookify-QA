package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UserEntity;

import java.util.Optional;

public interface UserSpringRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}