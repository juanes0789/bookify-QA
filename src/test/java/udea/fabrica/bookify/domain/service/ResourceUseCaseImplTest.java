package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ResourceRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateResourceRequest;

@ExtendWith(MockitoExtension.class)
class ResourceUseCaseImplTest {

    @Mock
    private ResourceRepositoryPort resourceRepositoryPort;

    @Mock
    private MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    @InjectMocks
    private ResourceUseCaseImpl useCase;

    private CreateResourceRequest request(String name, String type) {
        CreateResourceRequest request = new CreateResourceRequest();
        request.setName(name);
        request.setType(type);
        return request;
    }

    private MerchantProfile profile(UUID id, UUID userId) {
        return MerchantProfile.builder().id(id).userId(userId).businessName("Barbería Central").build();
    }

    @Test
    void shouldThrowForbidden_whenRoleIsCliente() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateResourceRequest request = request("Cancha 1", "FISICO");

        // Act + Assert
        assertThatThrownBy(() -> useCase.createResource(userId, "CLIENTE", request))
                .isInstanceOf(DomainForbiddenException.class);
        verifyNoInteractions(merchantProfileRepositoryPort, resourceRepositoryPort);
    }

    @Test
    void shouldThrowConflict_whenUserHasNoMerchantProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());
        CreateResourceRequest request = request("Cancha 1", "FISICO");

        // Act + Assert
        assertThatThrownBy(() -> useCase.createResource(userId, "PROVEEDOR", request))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("perfil comercial");
        verify(resourceRepositoryPort, never()).save(any(Resource.class));
    }

    @Test
    void shouldThrowBadRequest_whenTypeIsNotAllowed() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(profile(UUID.randomUUID(), userId)));
        CreateResourceRequest request = request("Cancha 1", "VIRTUAL");

        // Act + Assert
        assertThatThrownBy(() -> useCase.createResource(userId, "PROVEEDOR", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FISICO o HUMANO");
        verify(resourceRepositoryPort, never()).save(any(Resource.class));
    }

    @Test
    void shouldThrowConflict_whenNameAlreadyExistsInMerchant() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(profile(profileId, userId)));
        when(resourceRepositoryPort.existsByMerchantProfileIdAndNameIgnoreCase(profileId, "cancha 1"))
                .thenReturn(true);
        CreateResourceRequest request = request("cancha 1", "FISICO");

        // Act + Assert
        assertThatThrownBy(() -> useCase.createResource(userId, "PROVEEDOR", request))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("Ya existe un recurso");
        verify(resourceRepositoryPort, never()).save(any(Resource.class));
    }

    @Test
    void shouldSaveResourceWithProviderIdAndNormalizedType_whenRequestIsValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(profile(profileId, userId)));
        when(resourceRepositoryPort.existsByMerchantProfileIdAndNameIgnoreCase(profileId, "Ana"))
                .thenReturn(false);
        when(resourceRepositoryPort.save(any(Resource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Resource saved = useCase.createResource(userId, "proveedor", request("  Ana  ", " humano "));

        // Assert
        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().providerId()).isEqualTo(profileId);
        assertThat(captor.getValue().type()).isEqualTo("HUMANO");
        assertThat(captor.getValue().name()).isEqualTo("Ana");
        assertThat(captor.getValue().id()).isNull();
        assertThat(saved.name()).isEqualTo("Ana");
    }
}