package udea.fabrica.bookify.domain.port.in;

import java.util.UUID;

import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateResourceRequest;

public interface ResourceUseCase {
    Resource createResource(UUID userId, UUID userRole, CreateResourceRequest request);
}