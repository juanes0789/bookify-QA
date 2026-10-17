package udea.fabrica.bookify.domain.service;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.MerchantProfileUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

@Service
public class MerchantProfileUseCaseImpl implements MerchantProfileUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantProfileUseCaseImpl.class);

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String MERCHANT_ROLE = "PROVEEDOR";

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Barbería y belleza", "Deportes y recreación", "Salud y bienestar", "Educación", "Otros"
    );

    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    public MerchantProfileUseCaseImpl(MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    public MerchantProfile createProfile(Long userId, String userRole, CreateMerchantProfileRequest request) {
        if (!isMerchant(userRole)) {
            LOG.warn("createProfile rechazado: userId={}, userRole recibido='{}'", userId, userRole);
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

    private boolean isMerchant(String userRole) {
        if (userRole == null || userRole.isBlank()) {
            return false;
        }
        String normalized = userRole.trim().toUpperCase();
        if (normalized.startsWith(ROLE_PREFIX)) {
            normalized = normalized.substring(ROLE_PREFIX.length());
        }
        return MERCHANT_ROLE.equals(normalized);
    }
}