package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.ServiceOffer;
import udea.fabrica.bookify.domain.model.ServiceOfferAvailability;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GetServiceCatalogServiceTest {

    @Test
    void shouldGroupAvailableSchedulesByServiceAndProvider() {
        UUID serviceId = UUID.randomUUID();
        UUID firstAvailabilityId = UUID.randomUUID();
        UUID secondAvailabilityId = UUID.randomUUID();

        ServiceCatalogOutputPort outputPort = () -> List.of(
                new ServiceOfferAvailability(
                        serviceId,
                        "Corte de cabello",
                        "Barberia XYZ",
                        firstAvailabilityId,
                        OffsetDateTime.parse("2026-09-16T10:00:00Z"),
                        OffsetDateTime.parse("2026-09-16T10:30:00Z")
                ),
                new ServiceOfferAvailability(
                        serviceId,
                        "Corte de cabello",
                        "Barberia XYZ",
                        secondAvailabilityId,
                        OffsetDateTime.parse("2026-09-16T11:00:00Z"),
                        OffsetDateTime.parse("2026-09-16T11:30:00Z")
                )
        );

        List<ServiceOffer> result = new GetServiceCatalogService(outputPort).getAvailableServices();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getService()).isEqualTo("Corte de cabello");
        assertThat(result.get(0).getProvider()).isEqualTo("Barberia XYZ");
        assertThat(result.get(0).getAvailableSchedules())
                .extracting("availabilityId")
                .containsExactly(firstAvailabilityId, secondAvailabilityId);
    }
}
