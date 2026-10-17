package udea.fabrica.bookify.infrastructure.config;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityRepositoryPort;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.CategoryCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.CommonPasswordPort;
import udea.fabrica.bookify.domain.port.out.LoginAccessControlPort;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;
import udea.fabrica.bookify.domain.port.out.PasswordEncoderPort;
import udea.fabrica.bookify.domain.port.out.ResourceAvailabilityPort;
import udea.fabrica.bookify.domain.port.out.SearchServicesOutputPort;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;
import udea.fabrica.bookify.domain.service.BlockingDateService;
import udea.fabrica.bookify.domain.service.BookingService;
import udea.fabrica.bookify.domain.service.CreateAvailabilityService;
import udea.fabrica.bookify.domain.service.GetBookingHistoryService;
import udea.fabrica.bookify.domain.service.GetServiceCatalogService;
import udea.fabrica.bookify.domain.service.LoginService;
import udea.fabrica.bookify.domain.service.NonWorkingDayService;
import udea.fabrica.bookify.domain.service.RegisterUserService;
import udea.fabrica.bookify.domain.service.SearchServicesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BeanConfigurationTest {

    @Test
    void shouldCreateExpectedBeanImplementations() {
        // Arrange
        BeanConfiguration configuration = new BeanConfiguration();
        ServiceCatalogOutputPort serviceCatalogOutputPort = mock(ServiceCatalogOutputPort.class);
        BookingHistoryOutputPort bookingHistoryOutputPort = mock(BookingHistoryOutputPort.class);
        BookingRepositoryPort bookingRepositoryPort = mock(BookingRepositoryPort.class);
        AvailabilityRepositoryPort availabilityRepositoryPort = mock(AvailabilityRepositoryPort.class);

        // Act
        var catalog = configuration.getServiceCatalogInputPort(serviceCatalogOutputPort);
        var history = configuration.getBookingHistoryInputPort(bookingHistoryOutputPort);
        var booking = configuration.bookingService(bookingRepositoryPort, availabilityRepositoryPort);

        // Assert
        assertThat(catalog).isInstanceOf(GetServiceCatalogService.class);
        assertThat(history).isInstanceOf(GetBookingHistoryService.class);
        assertThat(booking).isInstanceOf(BookingService.class);
    }

    @Test
    void shouldCreateSprint2BeanImplementations() {
        // Arrange
        BeanConfiguration configuration = new BeanConfiguration();
        NonWorkingDayOutputPort nonWorkingDayOutputPort = mock(NonWorkingDayOutputPort.class);

        // Act
        var nonWorkingDay = configuration.registerNonWorkingDayInputPort(nonWorkingDayOutputPort);
        var availability = configuration.createAvailabilityInputPort(mock(AvailabilityOutputPort.class), nonWorkingDayOutputPort);
        var search = configuration.searchServicesInputPort(mock(SearchServicesOutputPort.class), mock(CategoryCatalogOutputPort.class));
        var booking = configuration.bookingService(mock(BookingRepositoryPort.class), mock(AvailabilityRepositoryPort.class),
                mock(ResourceAvailabilityPort.class));
        var login = configuration.loginUseCase(mock(LoginAccessControlPort.class), mock(LoginAuthenticationPort.class));
        var blockingDate = configuration.blockingDateService(mock(NonWorkingDayPort.class));
        var register = configuration.registerUserInputPort(mock(UserRepositoryPort.class), mock(PasswordEncoderPort.class),
                mock(CommonPasswordPort.class));

        // Assert
        assertThat(nonWorkingDay).isInstanceOf(NonWorkingDayService.class);
        assertThat(availability).isInstanceOf(CreateAvailabilityService.class);
        assertThat(search).isInstanceOf(SearchServicesService.class);
        assertThat(booking).isInstanceOf(BookingService.class);
        assertThat(login).isInstanceOf(LoginService.class);
        assertThat(blockingDate).isInstanceOf(BlockingDateService.class);
        assertThat(register).isInstanceOf(RegisterUserService.class);
    }
}
