package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateResourceRequest;

public interface ResourceUseCase {
    Resource createResource(Long userId, String userRole, CreateResourceRequest request);
}