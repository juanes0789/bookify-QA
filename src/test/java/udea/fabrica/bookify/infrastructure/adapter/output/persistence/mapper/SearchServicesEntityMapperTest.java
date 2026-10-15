package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.SearchServicesProjection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchServicesEntityMapperTest {

    @Test
    void shouldGroupAvailabilityRowsByServiceAndMapResource() {
        UUID serviceId = UUID.randomUUID();
        UUID availability1 = UUID.randomUUID();
        UUID availability2 = UUID.randomUUID();

        List<udea.fabrica.bookify.domain.model.ServiceSearchItem> result = SearchServicesEntityMapper.toDomain(List.of(
                row(serviceId, availability1, Instant.parse("2026-10-15T10:00:00Z"), "Recurso A"),
                row(serviceId, availability2, Instant.parse("2026-10-15T11:00:00Z"), "Recurso B")
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceId()).isEqualTo(serviceId);
        assertThat(result.get(0).getAvailableSchedules())
                .extracting(schedule -> schedule.getAvailabilityId())
                .containsExactly(availability1, availability2);
        assertThat(result.get(0).getAvailableSchedules().get(0).getRecurso()).isEqualTo("Recurso A");
        assertThat(result.get(0).getAvailableSchedules().get(0).getInicioAt().toString())
                .isEqualTo("2026-10-15T10:00Z");
    }

    private SearchServicesProjection row(UUID serviceId, UUID availabilityId, Instant inicioAt, String resource) {
        return new SearchServicesProjection() {
            @Override public UUID getServiceId() { return serviceId; }
            @Override public String getService() { return "Corte"; }
            @Override public String getProvider() { return "Barbería"; }
            @Override public String getCategory() { return "Belleza"; }
            @Override public UUID getAvailabilityId() { return availabilityId; }
            @Override public Instant getInicioAt() { return inicioAt; }
            @Override public Instant getFinAt() { return inicioAt.plusSeconds(1800); }
            @Override public String getResourceName() { return resource; }
        };
    }
}
