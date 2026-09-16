package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.model.AvailableSchedule;
import udea.fabrica.bookify.domain.model.ServiceOffer;
import udea.fabrica.bookify.domain.model.ServiceOfferAvailability;
import udea.fabrica.bookify.domain.port.in.GetServiceCatalogInputPort;
import udea.fabrica.bookify.domain.port.out.ServiceCatalogOutputPort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetServiceCatalogService implements GetServiceCatalogInputPort {

    private final ServiceCatalogOutputPort serviceCatalogOutputPort;

    public GetServiceCatalogService(ServiceCatalogOutputPort serviceCatalogOutputPort) {
        this.serviceCatalogOutputPort = serviceCatalogOutputPort;
    }

    @Override
    public List<ServiceOffer> getAvailableServices() {
        Map<String, ServiceOffer> offersByService = new LinkedHashMap<>();

        for (ServiceOfferAvailability availability : serviceCatalogOutputPort.findAvailableServiceSchedules()) {
            String key = availability.getServiceId() + "|" + availability.getProvider();
            ServiceOffer offer = offersByService.computeIfAbsent(key, ignored -> {
                ServiceOffer newOffer = new ServiceOffer();
                newOffer.setServiceId(availability.getServiceId());
                newOffer.setService(availability.getService());
                newOffer.setProvider(availability.getProvider());
                return newOffer;
            });
            offer.addAvailableSchedule(new AvailableSchedule(
                    availability.getAvailabilityId(),
                    availability.getInicioAt(),
                    availability.getFinAt()
            ));
        }

        return List.copyOf(offersByService.values());
    }
}
