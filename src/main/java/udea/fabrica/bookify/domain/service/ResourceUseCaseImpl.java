package udea.fabrica.bookify.domain.service;

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

    private final ResourceRepositoryPort resourceRepositoryPort;
    private final MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    public ResourceUseCaseImpl(ResourceRepositoryPort resourceRepositoryPort, 
                               MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        this.resourceRepositoryPort = resourceRepositoryPort;
        this.merchantProfileRepositoryPort = merchantProfileRepositoryPort;
    }

    @Override
    public Resource createResource(Long userId, String userRole, CreateResourceRequest request) {
        if (!"PROVEEDOR".equalsIgnoreCase(userRole)) {
            throw new DomainForbiddenException("Acceso no autorizado para el rol asignado");
        }

        MerchantProfile profile = merchantProfileRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new DomainConflictException("Primero debes crear tu perfil comercial"));

        String typeUpper = request.getType().toUpperCase();
        if (!"FISICO".equals(typeUpper) && !"HUMANO".equals(typeUpper)) {
            throw new IllegalArgumentException("El tipo de recurso debe ser FISICO o HUMANO");
        }

        if (resourceRepositoryPort.existsByMerchantProfileIdAndNameIgnoreCase(profile.getId(), request.getName().trim())) {
            throw new DomainConflictException("Ya existe un recurso con ese nombre en tu comercio");
        }

        Resource resource = Resource.builder()
                .merchantProfileId(profile.getId())
                .name(request.getName().trim())
                .type(typeUpper)
                .active(true)
                .build();

        return resourceRepositoryPort.save(resource);
    }
}