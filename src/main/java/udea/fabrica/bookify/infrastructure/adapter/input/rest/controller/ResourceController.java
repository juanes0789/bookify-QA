package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.in.ResourceUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateResourceRequest;

@RestController
@RequestMapping("/api/v1/comercio/recursos")
public class ResourceController {

    private final ResourceUseCase resourceUseCase;

    public ResourceController(ResourceUseCase resourceUseCase) {
        this.resourceUseCase = resourceUseCase;
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(
            @Valid @RequestBody CreateResourceRequest request,
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Resource resource = resourceUseCase.createResource(userId, role, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }
}