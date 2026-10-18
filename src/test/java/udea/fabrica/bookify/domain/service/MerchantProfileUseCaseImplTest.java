package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

@ExtendWith(MockitoExtension.class)
class MerchantProfileUseCaseImplTest {

    @Mock
    private MerchantProfileRepositoryPort repositoryPort;

    @InjectMocks
    private MerchantProfileUseCaseImpl useCase;

    private CreateMerchantProfileRequest validRequest() {
        CreateMerchantProfileRequest request = new CreateMerchantProfileRequest();
        request.setBusinessName("Barbería Central");
        request.setCategory("Barbería y belleza");
        request.setAddress("Calle 10 # 40-20");
        request.setPhone("+573001234567");
        return request;
    }

    @Test
    void shouldThrowForbidden_whenRoleIsCliente() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act + Assert
        assertThatThrownBy(() -> useCase.createProfile(userId, "CLIENTE", validRequest()))
                .isInstanceOf(DomainForbiddenException.class);
        verifyNoInteractions(repositoryPort);
    }

    @Test
    void shouldThrowForbidden_whenRoleIsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act + Assert
        assertThatThrownBy(() -> useCase.createProfile(userId, null, validRequest()))
                .isInstanceOf(DomainForbiddenException.class);
    }

    @Test
    void shouldThrowBadRequest_whenCategoryIsNotInCatalog() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateMerchantProfileRequest request = validRequest();
        request.setCategory("Barbería");

        // Act + Assert
        assertThatThrownBy(() -> useCase.createProfile(userId, "PROVEEDOR", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Categoría inválida");
        verify(repositoryPort, never()).save(any(MerchantProfile.class));
    }

    @Test
    void shouldThrowConflict_whenUserAlreadyHasProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(repositoryPort.existsByUserId(userId)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> useCase.createProfile(userId, "PROVEEDOR", validRequest()))
                .isInstanceOf(DomainConflictException.class);
        verify(repositoryPort, never()).save(any(MerchantProfile.class));
    }

    @Test
    void shouldReturnBadRequestBeforeConflict_whenCategoryIsInvalidAndProfileExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateMerchantProfileRequest request = validRequest();
        request.setCategory("Inventada");

        // Act + Assert
        assertThatThrownBy(() -> useCase.createProfile(userId, "PROVEEDOR", request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repositoryPort, never()).existsByUserId(any(UUID.class));
    }

    @Test
    void shouldSaveProfile_whenRoleIsProveedorInAnyCase() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(repositoryPort.existsByUserId(userId)).thenReturn(false);
        when(repositoryPort.save(any(MerchantProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MerchantProfile saved = useCase.createProfile(userId, "proveedor", validRequest());

        // Assert
        ArgumentCaptor<MerchantProfile> captor = ArgumentCaptor.forClass(MerchantProfile.class);
        verify(repositoryPort).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getBusinessName()).isEqualTo("Barbería Central");
        assertThat(captor.getValue().getCategory()).isEqualTo("Barbería y belleza");
        assertThat(captor.getValue().getAddress()).isEqualTo("Calle 10 # 40-20");
        assertThat(captor.getValue().getPhone()).isEqualTo("+573001234567");
        assertThat(saved.getBusinessName()).isEqualTo("Barbería Central");
    }
}