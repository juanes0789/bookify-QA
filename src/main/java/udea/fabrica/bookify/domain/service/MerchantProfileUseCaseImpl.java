package udea.fabrica.bookify.domain.service;

import org.springframework.stereotype.Service;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.MerchantProfileUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

import java.util.Set;

@Service
public class MerchantProfileUseCaseImpl implements MerchantProfileUseCase {

    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Barbería y belleza", "Deportes y recreación", "Salud y bienestar", "Educación", "Otros"
    );

    public MerchantProfileUseCaseImpl(MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    public MerchantProfile createProfile(Long userId, String userRole, CreateMerchantProfileRequest request) {
        if (!"PROVEEDOR".equalsIgnoreCase(userRole)) {
            throw new DomainForbiddenException("Acceso no autorizado para el rol asignado");
        }

        if (merchantProfileRepositoryPort.existsByUserId(userId)) {
            throw new DomainConflictException("Ya tienes un perfil comercial registrado");
        }

        if (!ALLOWED_CATEGORIES.contains(request.getCategory())) {
            throw new IllegalArgumentException("Categoría inválida. Las válidas son: " + ALLOWED_CATEGORIES);
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