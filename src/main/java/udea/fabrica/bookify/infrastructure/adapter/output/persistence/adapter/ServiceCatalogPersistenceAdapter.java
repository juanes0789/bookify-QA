package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.ServiceOfferAvailability;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AvailabilityJpaRepository;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class ServiceCatalogPersistenceAdapter implements ServiceCatalogOutputPort {

    private final AvailabilityJpaRepository availabilityJpaRepository;

    public ServiceCatalogPersistenceAdapter(AvailabilityJpaRepository availabilityJpaRepository) {
        this.availabilityJpaRepository = availabilityJpaRepository;
    }

    @Override
    public List<ServiceOfferAvailability> findAvailableServiceSchedules() {
        return availabilityJpaRepository.findAvailableServiceCatalog().stream()
                .map(row -> new ServiceOfferAvailability(
                        row.getServiceId(),
                        row.getService(),
                        row.getProvider(),
                        row.getAvailabilityId(),
                        row.getInicioAt().atOffset(ZoneOffset.UTC),
                        row.getFinAt().atOffset(ZoneOffset.UTC)
                ))
                .toList();
    }
}
