package udea.fabrica.bookify.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.in.GetBookingHistoryInputPort;
import udea.fabrica.bookify.domain.port.in.GetClientNoShowReportUseCase;
import udea.fabrica.bookify.domain.port.in.GetAttendanceMetricsUseCase;
import udea.fabrica.bookify.domain.port.in.GetServiceCatalogInputPort;
import udea.fabrica.bookify.domain.port.in.GetOccupancyMetricsUseCase;
import udea.fabrica.bookify.domain.port.in.ExportMetricsUseCase;
import udea.fabrica.bookify.domain.port.in.LoginUseCase;
import udea.fabrica.bookify.domain.port.in.RegisterAttendanceUseCase;
import udea.fabrica.bookify.domain.port.in.RegisterNonWorkingDayInputPort;
import udea.fabrica.bookify.domain.port.in.RegisterUserInputPort;
import udea.fabrica.bookify.domain.port.in.SearchServicesInputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityRepositoryPort;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;
import udea.fabrica.bookify.domain.port.out.ClientNoShowOutputPort;
import udea.fabrica.bookify.domain.port.out.AttendanceMetricsOutputPort;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.CategoryCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.CommonPasswordPort;
import udea.fabrica.bookify.domain.port.out.LoginAccessControlPort;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;
import udea.fabrica.bookify.domain.port.out.PasswordEncoderPort;
import udea.fabrica.bookify.domain.port.out.ResourceAvailabilityPort;
import udea.fabrica.bookify.domain.port.out.SearchServicesOutputPort;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;
import udea.fabrica.bookify.domain.port.out.OccupancyMetricsOutputPort;
import udea.fabrica.bookify.domain.port.out.BookingConfirmationNotificationPort;
import udea.fabrica.bookify.domain.service.BlockingDateService;
import udea.fabrica.bookify.domain.service.ClientNoShowService;
import udea.fabrica.bookify.domain.service.AttendanceMetricsService;
import udea.fabrica.bookify.domain.service.BookingAttendanceService;
import udea.fabrica.bookify.domain.service.BookingService;
import udea.fabrica.bookify.domain.service.CreateAvailabilityService;
import udea.fabrica.bookify.domain.service.GetBookingHistoryService;
import udea.fabrica.bookify.domain.service.GetServiceCatalogService;
import udea.fabrica.bookify.domain.service.LoginService;
import udea.fabrica.bookify.domain.service.NonWorkingDayService;
import udea.fabrica.bookify.domain.service.RegisterUserService;
import udea.fabrica.bookify.domain.service.SearchServicesService;
import udea.fabrica.bookify.domain.service.OccupancyMetricsService;
import udea.fabrica.bookify.domain.service.MetricsCsvExportService;

@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterNonWorkingDayInputPort registerNonWorkingDayInputPort(NonWorkingDayOutputPort nonWorkingDayOutputPort) {
        return new NonWorkingDayService(nonWorkingDayOutputPort);
    }

    @Bean
    public CreateAvailabilityInputPort createAvailabilityInputPort(AvailabilityOutputPort availabilityOutputPort,
                                                                    NonWorkingDayOutputPort nonWorkingDayOutputPort) {
        return new CreateAvailabilityService(availabilityOutputPort, nonWorkingDayOutputPort);
    }

    @Bean
    public GetServiceCatalogInputPort getServiceCatalogInputPort(ServiceCatalogOutputPort serviceCatalogOutputPort) {
        return new GetServiceCatalogService(serviceCatalogOutputPort);
    }

    @Bean
    public SearchServicesInputPort searchServicesInputPort(SearchServicesOutputPort searchServicesOutputPort,
                                                           CategoryCatalogOutputPort categoryCatalogOutputPort) {
        return new SearchServicesService(searchServicesOutputPort, categoryCatalogOutputPort);
    }

    @Bean
    public GetBookingHistoryInputPort getBookingHistoryInputPort(BookingHistoryOutputPort bookingHistoryOutputPort) {
        return new GetBookingHistoryService(bookingHistoryOutputPort);
    }

    @Bean
    public BookingService bookingService(BookingRepositoryPort bookingRepositoryPort,
                                         AvailabilityRepositoryPort availabilityRepositoryPort,
                                         ResourceAvailabilityPort resourceAvailabilityPort,
                                         BookingConfirmationNotificationPort confirmations) {
        return new BookingService(bookingRepositoryPort, availabilityRepositoryPort, resourceAvailabilityPort, confirmations);
    }

    /** Compatibility factory retained for existing callers and tests. */
    public BookingService bookingService(BookingRepositoryPort bookingRepositoryPort,
                                         AvailabilityRepositoryPort availabilityRepositoryPort,
                                         ResourceAvailabilityPort resourceAvailabilityPort) {
        return new BookingService(bookingRepositoryPort, availabilityRepositoryPort, resourceAvailabilityPort);
    }

    /** Compatibility factory retained for callers of the Sprint 1 API. */
    public BookingService bookingService(BookingRepositoryPort bookingRepositoryPort,
                                         AvailabilityRepositoryPort availabilityRepositoryPort) {
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

    @Bean
    public RegisterUserInputPort registerUserInputPort(UserRepositoryPort userRepositoryPort,
                                                       PasswordEncoderPort passwordEncoderPort,
                                                       CommonPasswordPort commonPasswordPort) {
        return new RegisterUserService(userRepositoryPort, passwordEncoderPort, commonPasswordPort);
    }

    @Bean
    public RegisterAttendanceUseCase registerAttendanceUseCase(
            BookingRepositoryPort bookingRepository,
            MerchantProfileRepositoryPort merchantProfileRepository) {
        return new BookingAttendanceService(bookingRepository, merchantProfileRepository);
    }

    @Bean
    public GetAttendanceMetricsUseCase getAttendanceMetricsUseCase(
            AttendanceMetricsOutputPort attendanceMetricsOutputPort,
            MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        return new AttendanceMetricsService(attendanceMetricsOutputPort, merchantProfileRepositoryPort);
    }

    @Bean
    public GetOccupancyMetricsUseCase getOccupancyMetricsUseCase(
            OccupancyMetricsOutputPort occupancyMetricsOutputPort,
            MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        return new OccupancyMetricsService(occupancyMetricsOutputPort, merchantProfileRepositoryPort);
    }

    @Bean
    public ExportMetricsUseCase exportMetricsUseCase(GetOccupancyMetricsUseCase occupancy,
                                                     GetAttendanceMetricsUseCase attendance) {
        return new MetricsCsvExportService(occupancy, attendance);
    }

    @Bean
    public GetClientNoShowReportUseCase getClientNoShowReportUseCase(
            ClientNoShowOutputPort clientNoShowOutputPort,
            MerchantProfileRepositoryPort merchantProfileRepositoryPort) {
        return new ClientNoShowService(clientNoShowOutputPort, merchantProfileRepositoryPort);
    }
}
