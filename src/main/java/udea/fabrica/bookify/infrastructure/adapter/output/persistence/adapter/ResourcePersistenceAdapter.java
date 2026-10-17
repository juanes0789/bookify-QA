package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.out.ResourceRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ResourceEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.ResourceSpringRepository;
import java.util.UUID;

@Component
public class ResourcePersistenceAdapter implements ResourceRepositoryPort {

    private final ResourceSpringRepository repository;

    public ResourcePersistenceAdapter(ResourceSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByMerchantProfileIdAndNameIgnoreCase(UUID merchantProfileId, String name) {
        return repository.existsByMerchantProfileIdAndNameIgnoreCase(merchantProfileId, name);
    }

    @Override
    public Resource save(Resource resource) {
        ResourceEntity entity = ResourceEntity.builder()
                .id_recurso(resource.id())
                .merchantProfileId(resource.providerId())
                .name(resource.name())
                .type(resource.resourceTypeId())
                .build();

        ResourceEntity saved = repository.save(entity);
        return new Resource(
            saved.getId_recurso(), 
            resource.providerId(), 
            resource.resourceTypeId(), 
            resource.name()
    );
    }
}