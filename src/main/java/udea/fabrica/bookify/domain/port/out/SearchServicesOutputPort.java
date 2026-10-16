package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.ServiceSearchItem;

import java.util.List;

public interface SearchServicesOutputPort {
    List<ServiceSearchItem> findAvailableServices(String texto, String categoria);
}
