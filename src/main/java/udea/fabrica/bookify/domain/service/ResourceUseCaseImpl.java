package udea.fabrica.bookify.domain.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.in.ResourceUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ResourceRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateResourceRequest;

@Service
public class ResourceUseCaseImpl implements ResourceUseCase {

    private static final String MERCHANT_ROLE = "PROVEEDOR";
    private static final Set<String> ALLOWED_TYPES = Set.of("FISICO", "HUMANO");

    private final ResourceRepositoryPort resourceRepositoryPort;
    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    public ResourceUseCaseImpl(ResourceRepositoryPort resourceRepositoryPort,
                               MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.resourceRepositoryPort = resourceRepositoryPort;
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    public Resource createResource(UUID userId, String userRole, CreateResourceRequest request) {
        if (!MERCHANT_ROLE.equalsIgnoreCase(userRole)) {
            throw new DomainForbiddenException("Acceso no autorizado para el rol asignado");
        }

        MerchantProfile profile = merchantProfileRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new DomainConflictException("Primero debes crear tu perfil comercial"));

        String type = request.getType().trim().toUpperCase();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException("El tipo de recurso debe ser FISICO o HUMANO");
        }

        String name = request.getName().trim();
        if (resourceRepositoryPort.existsByMerchantProfileIdAndNameIgnoreCase(profile.getId(), name)) {
            throw new DomainConflictException("Ya existe un recurso con ese nombre en tu comercio");
        }

        Resource resource = Resource.builder()
                .providerId(profile.getId())
                .type(type)
                .name(name)
                .build();

        return resourceRepositoryPort.save(resource);
    }
}