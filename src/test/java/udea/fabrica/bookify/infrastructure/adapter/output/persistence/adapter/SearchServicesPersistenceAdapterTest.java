package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.SearchServicesJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.SearchServicesProjection;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.TipoProveedorJpaRepository;

@ExtendWith(MockitoExtension.class)
class SearchServicesPersistenceAdapterTest {
    @Mock private SearchServicesJpaRepository searchServicesJpaRepository;
    @Mock private TipoProveedorJpaRepository tipoProveedorJpaRepository;
    @InjectMocks private SearchServicesPersistenceAdapter adapter;

    @Test
    void shouldGroupSchedulesByService_whenRepositoryReturnsRows() {
        // Arrange
        UUID serviceId = UUID.randomUUID();
        Instant start = Instant.parse("2026-10-10T15:00:00Z");
        SearchServicesProjection carlos = row(serviceId, start, "Barbero Carlos");
        SearchServicesProjection andres = row(serviceId, start, "Barbero Andrés");
        when(searchServicesJpaRepository.findAvailableServices("corte", "Barbería y belleza"))
                .thenReturn(List.of(carlos, andres));

        // Act
        List<ServiceSearchItem> result = adapter.findAvailableServices("corte", "Barbería y belleza");

        // Assert
        assertThat(result).hasSize(1);
        ServiceSearchItem item = result.get(0);
        assertThat(item.getService()).isEqualTo("Corte de cabello");
        assertThat(item.getProvider()).isEqualTo("Barbería Central");
        assertThat(item.getCategory()).isEqualTo("Barbería y belleza");
        assertThat(item.getAvailableSchedules()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyList_whenNoServiceMatches() {
        // Arrange
        when(searchServicesJpaRepository.findAvailableServices("yoga", null)).thenReturn(List.of());

        // Act
        List<ServiceSearchItem> result = adapter.findAvailableServices("yoga", null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDelegateCategoryLookup_whenCategoryIsChecked() {
        // Arrange
        when(tipoProveedorJpaRepository.existsByNombreIgnoreCase("Deportes y recreación")).thenReturn(true);

        // Act
        boolean exists = adapter.existsCategoryByName("Deportes y recreación");

        // Assert
        assertThat(exists).isTrue();
        verify(tipoProveedorJpaRepository).existsByNombreIgnoreCase("Deportes y recreación");
    }

    private static SearchServicesProjection row(UUID serviceId, Instant start, String resourceName) {
        // Las filas repetidas de un servicio solo aportan horarios: el mapper no lee sus datos de servicio.
        SearchServicesProjection projection = mock(SearchServicesProjection.class,
                withSettings().strictness(Strictness.LENIENT));
        when(projection.getServiceId()).thenReturn(serviceId);
        when(projection.getService()).thenReturn("Corte de cabello");
        when(projection.getProvider()).thenReturn("Barbería Central");
        when(projection.getCategory()).thenReturn("Barbería y belleza");
        when(projection.getAvailabilityId()).thenReturn(UUID.randomUUID());
        when(projection.getInicioAt()).thenReturn(start);
        when(projection.getFinAt()).thenReturn(start.plusSeconds(3600));
        when(projection.getResourceName()).thenReturn(resourceName);
        return projection;
    }
}
