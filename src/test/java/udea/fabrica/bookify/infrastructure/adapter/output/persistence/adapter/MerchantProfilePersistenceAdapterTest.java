package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.MerchantProfileEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.MerchantProfileSpringRepository;

@ExtendWith(MockitoExtension.class)
class MerchantProfilePersistenceAdapterTest {
    @Mock private MerchantProfileSpringRepository repository;
    @InjectMocks private MerchantProfilePersistenceAdapter adapter;

    @Test
    void shouldReportExistingProfile_whenUserAlreadyHasOne() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(repository.existsByUserId(userId)).thenReturn(true);

        // Act
        boolean exists = adapter.existsByUserId(userId);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldMapAllFieldsAndReturnGeneratedId_whenProfileIsSaved() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID generatedId = UUID.randomUUID();
        MerchantProfile profile = MerchantProfile.builder()
                .userId(userId)
                .businessName("Barbería Central")
                .category("Barbería y belleza")
                .address("Calle 10 # 43-20, Medellín")
                .phone("+573001234567")
                .build();
        when(repository.save(any(MerchantProfileEntity.class))).thenAnswer(invocation -> {
            MerchantProfileEntity entity = invocation.getArgument(0);
            entity.setId(generatedId);
            return entity;
        });

        // Act
        MerchantProfile saved = adapter.save(profile);

        // Assert
        ArgumentCaptor<MerchantProfileEntity> captor = ArgumentCaptor.forClass(MerchantProfileEntity.class);
        verify(repository).save(captor.capture());
        MerchantProfileEntity entity = captor.getValue();
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getBusinessName()).isEqualTo("Barbería Central");
        assertThat(entity.getCategory()).isEqualTo("Barbería y belleza");
        assertThat(entity.getAddress()).isEqualTo("Calle 10 # 43-20, Medellín");
        assertThat(entity.getPhone()).isEqualTo("+573001234567");
        assertThat(saved.getId()).isEqualTo(generatedId);
    }

    @Test
    void shouldMapEntityToDomain_whenProfileIsFoundByUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        MerchantProfileEntity entity = new MerchantProfileEntity(profileId, userId, "Complejo El Estadio",
                "Deportes y recreación", "Carrera 70 # 1-10", "+573104567890");
        when(repository.findByUserId(userId)).thenReturn(Optional.of(entity));

        // Act
        Optional<MerchantProfile> result = adapter.findByUserId(userId);

        // Assert
        assertThat(result).contains(new MerchantProfile(profileId, userId, "Complejo El Estadio",
                "Deportes y recreación", "Carrera 70 # 1-10", "+573104567890"));
    }

    @Test
    void shouldReturnEmpty_whenUserHasNoProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        Optional<MerchantProfile> result = adapter.findByUserId(userId);

        // Assert
        assertThat(result).isEmpty();
    }
}
