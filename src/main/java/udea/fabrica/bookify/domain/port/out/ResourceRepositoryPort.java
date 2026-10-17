package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.Resource;

public interface ResourceRepositoryPort {
    boolean existsByMerchantProfileIdAndNameIgnoreCase(Long merchantProfileId, String name);
    Resource save(Resource resource);
}