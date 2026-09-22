package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.AvailableSchedule;
import udea.fabrica.bookify.domain.model.ServiceOffer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceCatalogRestMapperTest {

    @Test
    void shouldMapServiceOffersToResponseListWithSchedules() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        ServiceOffer offer = new ServiceOffer(
                UUID.randomUUID(),
                "Corte",
                "Barberia",
                List.of(new AvailableSchedule(
                        scheduleId,
                        OffsetDateTime.parse("2026-10-06T10:00:00Z"),
                        OffsetDateTime.parse("2026-10-06T10:30:00Z")
                ))
        );

        // Act
        var result = ServiceCatalogRestMapper.toResponse(List.of(offer));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getService()).isEqualTo("Corte");
        assertThat(result.get(0).getProvider()).isEqualTo("Barberia");
        assertThat(result.get(0).getAvailableSchedules()).hasSize(1);
        assertThat(result.get(0).getAvailableSchedules().get(0).getAvailabilityId()).isEqualTo(scheduleId);
    }
}
