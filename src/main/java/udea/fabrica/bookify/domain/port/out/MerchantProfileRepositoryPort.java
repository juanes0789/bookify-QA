package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.MerchantProfile;
import java.util.Optional;

public interface MerchantProfileRepositoryPort {
    boolean existsByUserId(Long userId);
    MerchantProfile save(MerchantProfile profile);
    Optional<MerchantProfile> findByUserId(Long userId);
}