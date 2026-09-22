package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.AvailableSchedule;
import udea.fabrica.bookify.domain.model.ServiceOffer;
import udea.fabrica.bookify.domain.port.in.GetServiceCatalogInputPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CatalogControllerTest {

    @Test
    void shouldReturnCatalogMappedAsResponseDtos() {
        // Arrange
        GetServiceCatalogInputPort inputPort = mock(GetServiceCatalogInputPort.class);
        CatalogController controller = new CatalogController(inputPort);
        UUID availabilityId = UUID.randomUUID();

        ServiceOffer offer = new ServiceOffer();
        offer.setServiceId(UUID.randomUUID());
        offer.setService("Manicure");
        offer.setProvider("Nails Co");
        offer.setAvailableSchedules(List.of(new AvailableSchedule(
                availabilityId,
                OffsetDateTime.parse("2026-10-03T09:00:00Z"),
                OffsetDateTime.parse("2026-10-03T10:00:00Z")
        )));
        when(inputPort.getAvailableServices()).thenReturn(List.of(offer));

        // Act
        var response = controller.getAvailableServices();

        // Assert
        verify(inputPort).getAvailableServices();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getService()).isEqualTo("Manicure");
        assertThat(response.getBody().get(0).getAvailableSchedules().get(0).getAvailabilityId())
                .isEqualTo(availabilityId);
    }
}
