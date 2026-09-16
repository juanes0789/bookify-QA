package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.ServiceOfferAvailability;

import java.util.List;

public interface ServiceCatalogOutputPort {
    List<ServiceOfferAvailability> findAvailableServiceSchedules();
}
