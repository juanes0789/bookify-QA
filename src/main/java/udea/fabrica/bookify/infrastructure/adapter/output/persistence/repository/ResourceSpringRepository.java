package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ResourceEntity;
import java.util.UUID;

public interface ResourceSpringRepository extends JpaRepository<ResourceEntity, UUID> {
    boolean existsByMerchantProfileIdAndNameIgnoreCase(UUID merchantProfileId, String name);
}