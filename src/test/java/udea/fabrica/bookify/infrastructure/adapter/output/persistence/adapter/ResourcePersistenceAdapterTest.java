package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ResourceEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.ResourceSpringRepository;

@ExtendWith(MockitoExtension.class)
class ResourcePersistenceAdapterTest {
    @Mock private ResourceSpringRepository repository;
    @InjectMocks private ResourcePersistenceAdapter adapter;

    @Test
    void shouldDetectDuplicateName_whenMerchantAlreadyHasResource() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        when(repository.existsByMerchantProfileIdAndNameIgnoreCase(merchantId, "cancha 1")).thenReturn(true);

        // Act
        boolean exists = adapter.existsByMerchantProfileIdAndNameIgnoreCase(merchantId, "cancha 1");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldMapFieldsAndReturnGeneratedId_whenResourceIsSaved() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        UUID generatedId = UUID.randomUUID();
        Resource resource = new Resource(null, providerId, typeId, "Cancha 1");
        when(repository.save(any(ResourceEntity.class))).thenAnswer(invocation -> {
            ResourceEntity entity = invocation.getArgument(0);
            entity.setId_recurso(generatedId);
            return entity;
        });

        // Act
        Resource saved = adapter.save(resource);

        // Assert
        ArgumentCaptor<ResourceEntity> captor = ArgumentCaptor.forClass(ResourceEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMerchantProfileId()).isEqualTo(providerId);
        assertThat(captor.getValue().getType()).isEqualTo(typeId);
        assertThat(captor.getValue().getName()).isEqualTo("Cancha 1");
        assertThat(saved).isEqualTo(new Resource(generatedId, providerId, typeId, "Cancha 1"));
    }
}
