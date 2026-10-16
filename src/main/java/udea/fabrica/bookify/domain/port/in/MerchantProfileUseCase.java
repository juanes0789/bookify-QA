package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

public interface MerchantProfileUseCase {
    MerchantProfile createProfile(Long userId, String userRole, CreateMerchantProfileRequest request);
}