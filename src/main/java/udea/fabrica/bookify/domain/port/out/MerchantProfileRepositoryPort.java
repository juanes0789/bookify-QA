package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.MerchantProfile;
import java.util.Optional;
import java.util.UUID;

public interface MerchantProfileRepositoryPort {
    boolean existsByUserId(UUID userId);
    MerchantProfile save(MerchantProfile profile);
    Optional<MerchantProfile> findByUserId(UUID userId);
}