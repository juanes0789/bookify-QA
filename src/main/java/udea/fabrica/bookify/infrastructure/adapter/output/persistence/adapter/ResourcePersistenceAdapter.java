package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.out.ResourceRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ResourceEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.ResourceSpringRepository;

@Component
public class ResourcePersistenceAdapter implements ResourceRepositoryPort {

    private final ResourceSpringRepository repository;

    public ResourcePersistenceAdapter(ResourceSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByMerchantProfileIdAndNameIgnoreCase(Long merchantProfileId, String name) {
        return repository.existsByMerchantProfileIdAndNameIgnoreCase(merchantProfileId, name);
    }

    @Override
    public Resource save(Resource resource) {
        ResourceEntity entity = ResourceEntity.builder()
                .id(resource.getId())
                .merchantProfileId(resource.getMerchantProfileId())
                .name(resource.getName())
                .type(resource.getType())
                .active(resource.isActive())
                .build();

        ResourceEntity saved = repository.save(entity);
        resource.setId(saved.getId());
        return resource;
    }
}