package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.exception.CategoryNotFoundException;
import udea.fabrica.bookify.domain.exception.InvalidSearchCriteriaException;
import udea.fabrica.bookify.domain.model.SearchServicesResult;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.domain.port.in.SearchServicesInputPort;
import udea.fabrica.bookify.domain.port.out.CategoryCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.SearchServicesOutputPort;
import udea.fabrica.bookify.domain.util.TextNormalizer;

import java.util.List;

public class SearchServicesService implements SearchServicesInputPort {

    private static final String EMPTY_SEARCH_MESSAGE = "Debe ingresar al menos un criterio de b\u00fasqueda: texto o categor\u00eda";
    private static final String NO_RESULTS_MESSAGE = "No se encontraron servicios";

    private final SearchServicesOutputPort searchServicesOutputPort;
    private final CategoryCatalogOutputPort categoryCatalogOutputPort;

    public SearchServicesService(SearchServicesOutputPort searchServicesOutputPort,
                                 CategoryCatalogOutputPort categoryCatalogOutputPort) {
        this.searchServicesOutputPort = searchServicesOutputPort;
        this.categoryCatalogOutputPort = categoryCatalogOutputPort;
    }

    @Override
    public SearchServicesResult search(String texto, String categoria) {
        String cleanText = clean(texto);
        String cleanCategory = clean(categoria);

        if (cleanText == null && cleanCategory == null) {
            throw new InvalidSearchCriteriaException(EMPTY_SEARCH_MESSAGE);
        }

        if (cleanCategory != null && !categoryCatalogOutputPort.existsCategoryByName(cleanCategory)) {
            throw new CategoryNotFoundException("La categor\u00eda ingresada '" + cleanCategory + "' no existe en el cat\u00e1logo");
        }

        List<ServiceSearchItem> services = searchServicesOutputPort.findAvailableServices(cleanText, cleanCategory).stream()
                .filter(item -> matchesText(item, cleanText))
                .filter(item -> matchesCategory(item, cleanCategory))
                .filter(item -> item.getAvailableSchedules() != null && !item.getAvailableSchedules().isEmpty())
                .toList();

        if (services.isEmpty()) {
            return new SearchServicesResult(List.of(), NO_RESULTS_MESSAGE);
        }
        return new SearchServicesResult(services, null);
    }

    private boolean matchesText(ServiceSearchItem item, String text) {
        if (text == null) {
            return true;
        }
        return TextNormalizer.containsIgnoreCaseAndAccents(item.getService(), text)
                || TextNormalizer.containsIgnoreCaseAndAccents(item.getProvider(), text);
    }

    private boolean matchesCategory(ServiceSearchItem item, String category) {
        if (category == null) {
            return true;
        }
        return TextNormalizer.normalize(category).equals(TextNormalizer.normalize(item.getCategory()));
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
