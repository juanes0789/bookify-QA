package udea.fabrica.bookify.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.in.GetBookingHistoryInputPort;
import udea.fabrica.bookify.domain.port.in.GetServiceCatalogInputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;
import udea.fabrica.bookify.domain.service.CreateAvailabilityService;
import udea.fabrica.bookify.domain.service.GetBookingHistoryService;
import udea.fabrica.bookify.domain.service.GetServiceCatalogService;
import udea.fabrica.bookify.domain.port.in.CreateBookingUseCase;
import udea.fabrica.bookify.domain.port.in.CancelBookingUseCase;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityRepositoryPort;
import udea.fabrica.bookify.domain.service.BookingService;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateAvailabilityInputPort createAvailabilityInputPort(AvailabilityOutputPort availabilityOutputPort) {
        return new CreateAvailabilityService(availabilityOutputPort);
    }

    @Bean
    public GetServiceCatalogInputPort getServiceCatalogInputPort(ServiceCatalogOutputPort serviceCatalogOutputPort) {
        return new GetServiceCatalogService(serviceCatalogOutputPort);
    }

    @Bean
    public GetBookingHistoryInputPort getBookingHistoryInputPort(BookingHistoryOutputPort bookingHistoryOutputPort) {
        return new GetBookingHistoryService(bookingHistoryOutputPort);
    }

    @Bean public BookingService bookingService(BookingRepositoryPort b, AvailabilityRepositoryPort a) {
        return new BookingService(b, a);
    }
    @Bean public CreateBookingUseCase createBookingUseCase(BookingService service) { return service; }
    @Bean public CancelBookingUseCase cancelBookingUseCase(BookingService service) { return service; }

}
