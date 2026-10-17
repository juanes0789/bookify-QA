package udea.fabrica.bookify.domain.port.in;

import java.util.UUID;

import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

public interface MerchantProfileUseCase {
    MerchantProfile createProfile(UUID userId, UUID userRole, CreateMerchantProfileRequest request);
}