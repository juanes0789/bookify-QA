package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.MerchantProfileEntity;

import java.util.Optional;

public interface MerchantProfileSpringRepository extends JpaRepository<MerchantProfileEntity, Long> {
    boolean existsByUserId(Long userId);
    Optional<MerchantProfileEntity> findByUserId(Long userId);
}