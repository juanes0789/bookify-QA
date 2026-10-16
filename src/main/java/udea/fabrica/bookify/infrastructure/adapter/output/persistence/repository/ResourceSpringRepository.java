package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ResourceEntity;

public interface ResourceSpringRepository extends JpaRepository<ResourceEntity, Long> {
    boolean existsByMerchantProfileIdAndNameIgnoreCase(Long merchantProfileId, String name);
}