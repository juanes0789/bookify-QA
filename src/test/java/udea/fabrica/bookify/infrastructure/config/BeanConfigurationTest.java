package udea.fabrica.bookify.infrastructure.config;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityRepositoryPort;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;
import udea.fabrica.bookify.domain.service.BookingService;
import udea.fabrica.bookify.domain.service.CreateAvailabilityService;
import udea.fabrica.bookify.domain.service.GetBookingHistoryService;
import udea.fabrica.bookify.domain.service.GetServiceCatalogService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BeanConfigurationTest {

    @Test
    void shouldCreateExpectedBeanImplementations() {
        // Arrange
        BeanConfiguration configuration = new BeanConfiguration();
        AvailabilityOutputPort availabilityOutputPort = mock(AvailabilityOutputPort.class);
        ServiceCatalogOutputPort serviceCatalogOutputPort = mock(ServiceCatalogOutputPort.class);
        BookingHistoryOutputPort bookingHistoryOutputPort = mock(BookingHistoryOutputPort.class);
        BookingRepositoryPort bookingRepositoryPort = mock(BookingRepositoryPort.class);
        AvailabilityRepositoryPort availabilityRepositoryPort = mock(AvailabilityRepositoryPort.class);

        // Act
        var createAvailability = configuration.createAvailabilityInputPort(availabilityOutputPort);
        var catalog = configuration.getServiceCatalogInputPort(serviceCatalogOutputPort);
        var history = configuration.getBookingHistoryInputPort(bookingHistoryOutputPort);
        var booking = configuration.bookingService(bookingRepositoryPort, availabilityRepositoryPort);

        // Assert
        assertThat(createAvailability).isInstanceOf(CreateAvailabilityService.class);
        assertThat(catalog).isInstanceOf(GetServiceCatalogService.class);
        assertThat(history).isInstanceOf(GetBookingHistoryService.class);
        assertThat(booking).isInstanceOf(BookingService.class);
    }
}
