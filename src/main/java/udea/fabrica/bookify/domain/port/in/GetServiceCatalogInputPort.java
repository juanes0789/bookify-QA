package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.ServiceOffer;

import java.util.List;

public interface GetServiceCatalogInputPort {
    List<ServiceOffer> getAvailableServices();
}
