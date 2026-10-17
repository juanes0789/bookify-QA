package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.Resource;
import java.util.UUID;

public interface ResourceRepositoryPort {
    boolean existsByMerchantProfileIdAndNameIgnoreCase(UUID merchantProfileId, String name);
    Resource save(Resource resource);
}