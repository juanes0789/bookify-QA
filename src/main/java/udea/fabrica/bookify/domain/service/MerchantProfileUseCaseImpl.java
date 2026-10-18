package udea.fabrica.bookify.domain.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.MerchantProfileUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

@Service
public class MerchantProfileUseCaseImpl implements MerchantProfileUseCase {

    private static final String MERCHANT_ROLE = "PROVEEDOR";

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Barbería y belleza", "Deportes y recreación", "Salud y bienestar", "Educación", "Otros"
    );

    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    public MerchantProfileUseCaseImpl(MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    public MerchantProfile createProfile(UUID userId, String userRole, CreateMerchantProfileRequest request) {
        if (!MERCHANT_ROLE.equalsIgnoreCase(userRole)) {
            throw new DomainForbiddenException("Acceso no autorizado para el rol asignado");
        }

        if (!ALLOWED_CATEGORIES.contains(request.getCategory())) {
            throw new IllegalArgumentException("Categoría inválida. Las válidas son: " + ALLOWED_CATEGORIES);
        }

        if (merchantProfileRepositoryPort.existsByUserId(userId)) {
            throw new DomainConflictException("Ya tienes un perfil comercial registrado");
        }

        MerchantProfile profile = MerchantProfile.builder()
                .userId(userId)
                .businessName(request.getBusinessName())
                .category(request.getCategory())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return merchantProfileRepositoryPort.save(profile);
    }
}