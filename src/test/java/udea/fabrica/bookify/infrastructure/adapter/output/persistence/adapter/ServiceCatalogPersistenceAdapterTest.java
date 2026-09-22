package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AvailabilityJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.ServiceCatalogProjection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ServiceCatalogPersistenceAdapterTest {

    @Test
    void shouldMapRepositoryProjectionToDomainRows() {
        // Arrange
        AvailabilityJpaRepository repository = mock(AvailabilityJpaRepository.class);
        ServiceCatalogPersistenceAdapter adapter = new ServiceCatalogPersistenceAdapter(repository);
        ServiceCatalogProjection projection = mock(ServiceCatalogProjection.class);
        UUID serviceId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        when(projection.getServiceId()).thenReturn(serviceId);
        when(projection.getService()).thenReturn("Corte");
        when(projection.getProvider()).thenReturn("Barberia");
        when(projection.getAvailabilityId()).thenReturn(availabilityId);
        when(projection.getInicioAt()).thenReturn(Instant.parse("2026-10-10T10:00:00Z"));
        when(projection.getFinAt()).thenReturn(Instant.parse("2026-10-10T10:30:00Z"));
        when(repository.findAvailableServiceCatalog()).thenReturn(List.of(projection));

        // Act
        var result = adapter.findAvailableServiceSchedules();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceId()).isEqualTo(serviceId);
        assertThat(result.get(0).getAvailabilityId()).isEqualTo(availabilityId);
        assertThat(result.get(0).getInicioAt().toString()).isEqualTo("2026-10-10T10:00Z");
    }
}
