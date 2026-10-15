package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.SearchServicesResult;

public interface SearchServicesInputPort {
    SearchServicesResult search(String texto, String categoria);
}
