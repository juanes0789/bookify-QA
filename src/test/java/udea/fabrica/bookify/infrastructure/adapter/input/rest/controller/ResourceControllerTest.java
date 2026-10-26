package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.in.ResourceUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateResourceRequest;

class ResourceControllerTest {

    private ResourceUseCase useCase;
    private ResourceController controller;

    @BeforeEach
    void setUp() {
        useCase = mock(ResourceUseCase.class);
        controller = new ResourceController(useCase);
    }

    @Test
    void shouldReturn201WithResource_whenProviderCreatesResource() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), "token", List.of(new SimpleGrantedAuthority("PROVEEDOR")));
        CreateResourceRequest request = new CreateResourceRequest();
        request.setName("Cancha 1");
        request.setType("FISICO");
        Resource created = Resource.builder()
                .id(UUID.randomUUID()).providerId(UUID.randomUUID())
                .type("FISICO").name("Cancha 1").status("ACTIVO").build();
        when(useCase.createResource(userId, "PROVEEDOR", request)).thenReturn(created);

        // Act
        ResponseEntity<Resource> response = controller.createResource(request, authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
    }
}
