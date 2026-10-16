package udea.fabrica.bookify.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.in.GetBookingHistoryInputPort;
import udea.fabrica.bookify.domain.port.in.GetServiceCatalogInputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityRepositoryPort;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.ResourceAvailabilityPort;
import udea.fabrica.bookify.domain.port.out.LoginAccessControlPort;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;
import udea.fabrica.bookify.domain.service.BookingService;
import udea.fabrica.bookify.domain.service.CreateAvailabilityService;
import udea.fabrica.bookify.domain.service.GetBookingHistoryService;
import udea.fabrica.bookify.domain.service.GetServiceCatalogService;
import udea.fabrica.bookify.domain.service.LoginService;
import udea.fabrica.bookify.domain.service.BlockingDateService;
import udea.fabrica.bookify.domain.port.in.LoginUseCase;

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

    @Bean
    public BookingService bookingService(BookingRepositoryPort bookingRepositoryPort, AvailabilityRepositoryPort availabilityRepositoryPort, ResourceAvailabilityPort resourceAvailabilityPort) {
        return new BookingService(bookingRepositoryPort, availabilityRepositoryPort, resourceAvailabilityPort);
    }

    /** Compatibility factory retained for callers of the Sprint 1 API. */
    public BookingService bookingService(BookingRepositoryPort bookingRepositoryPort, AvailabilityRepositoryPort availabilityRepositoryPort) {
        return new BookingService(bookingRepositoryPort, availabilityRepositoryPort);
    }

    @Bean 
    public LoginUseCase loginUseCase(LoginAccessControlPort controls, LoginAuthenticationPort authentication) { 
        return new LoginService(controls, authentication); 
    }
    
    @Bean 
    public BlockingDateService blockingDateService(NonWorkingDayPort port) { 
        return new BlockingDateService(port); 
    }
}