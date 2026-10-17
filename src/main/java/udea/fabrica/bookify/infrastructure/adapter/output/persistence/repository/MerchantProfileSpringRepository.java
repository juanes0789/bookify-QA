package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.MerchantProfileEntity;

import java.util.Optional;
import java.util.UUID;

public interface MerchantProfileSpringRepository extends JpaRepository<MerchantProfileEntity, UUID> {
    boolean existsByUserId(UUID userId);
    Optional<MerchantProfileEntity> findByUserId(UUID userId);
}