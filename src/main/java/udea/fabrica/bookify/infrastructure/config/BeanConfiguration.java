package udea.fabrica.bookify.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.service.CreateAvailabilityService;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateAvailabilityInputPort createAvailabilityInputPort(AvailabilityOutputPort availabilityOutputPort) {
        return new CreateAvailabilityService(availabilityOutputPort);
    }
}