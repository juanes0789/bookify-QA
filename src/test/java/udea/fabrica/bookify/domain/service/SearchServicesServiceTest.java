package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.exception.CategoryNotFoundException;
import udea.fabrica.bookify.domain.exception.InvalidSearchCriteriaException;
import udea.fabrica.bookify.domain.model.AvailableScheduleWithResource;
import udea.fabrica.bookify.domain.model.SearchServicesResult;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.domain.port.out.CategoryCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.SearchServicesOutputPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchServicesServiceTest {

    private FakeSearchOutput searchOutput;
    private FakeCategoryCatalog categoryCatalog;
    private SearchServicesService service;

    @BeforeEach
    void setUp() {
        searchOutput = new FakeSearchOutput();
        categoryCatalog = new FakeCategoryCatalog();
        service = new SearchServicesService(searchOutput, categoryCatalog);
    }

    @Test
    void shouldRejectSearchWithoutCriteria() {
        assertThatThrownBy(() -> service.search("  ", null))
                .isInstanceOf(InvalidSearchCriteriaException.class);
    }

    @Test
    void shouldRejectCategoryOutsideCatalog() {
        assertThatThrownBy(() -> service.search(null, "Categoría inválida"))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(searchOutput.called).isFalse();
    }

    @Test
    void shouldFindServiceByPartialAccentInsensitiveServiceName() {
        searchOutput.results = List.of(offer("Fútbol 5", "Complejo", "Deportes y recreación", true));

        SearchServicesResult result = service.search("FUTBOL", null);

        assertThat(result.getServicios()).hasSize(1);
        assertThat(result.getMensaje()).isNull();
    }

    @Test
    void shouldFindServiceByProviderName() {
        searchOutput.results = List.of(offer("Corte", "Barbería Central", "Belleza", true));

        SearchServicesResult result = service.search("barberia central", null);

        assertThat(result.getServicios()).hasSize(1);
    }

    @Test
    void shouldApplyTextAndCategoryTogether() {
        categoryCatalog.exists = true;
        searchOutput.results = List.of(
                offer("Fútbol 5", "Cancha", "Deportes y recreación", true),
                offer("Fútbol 5", "Club", "Entretenimiento", true),
                offer("Natación", "Centro", "Deportes y recreación", true)
        );

        SearchServicesResult result = service.search("futbol", "Deportes y recreacion");

        assertThat(result.getServicios()).singleElement()
                .satisfies(item -> assertThat(item.getCategory()).isEqualTo("Deportes y recreación"));
        assertThat(searchOutput.texto).isEqualTo("futbol");
        assertThat(searchOutput.categoria).isEqualTo("Deportes y recreacion");
    }

    @Test
    void shouldExcludeOffersWithoutSchedules() {
        searchOutput.results = List.of(
                offer("Corte", "Barbería", "Belleza", true),
                offer("Tinte", "Barbería", "Belleza", false)
        );

        SearchServicesResult result = service.search("barberia", null);

        assertThat(result.getServicios()).hasSize(1);
        assertThat(result.getServicios().get(0).getService()).isEqualTo("Corte");
    }

    @Test
    void shouldReturnEmptyMessageWhenNoOffersMatch() {
        searchOutput.results = List.of();

        SearchServicesResult result = service.search("yoga", null);

        assertThat(result.getServicios()).isEmpty();
        assertThat(result.getMensaje()).isEqualTo("No se encontraron servicios");
    }

    private ServiceSearchItem offer(String name, String provider, String category, boolean hasSchedule) {
        ServiceSearchItem item = new ServiceSearchItem();
        item.setServiceId(UUID.randomUUID());
        item.setService(name);
        item.setProvider(provider);
        item.setCategory(category);
        if (hasSchedule) {
            OffsetDateTime start = OffsetDateTime.parse("2026-10-15T10:00:00Z");
            item.addAvailableSchedule(new AvailableScheduleWithResource(UUID.randomUUID(), start, start.plusMinutes(30), "Recurso"));
        }
        return item;
    }

    private static class FakeSearchOutput implements SearchServicesOutputPort {
        private List<ServiceSearchItem> results = List.of();
        private boolean called;
        private String texto;
        private String categoria;

        @Override
        public List<ServiceSearchItem> findAvailableServices(String texto, String categoria) {
            this.called = true;
            this.texto = texto;
            this.categoria = categoria;
            return results;
        }
    }

    private static class FakeCategoryCatalog implements CategoryCatalogOutputPort {
        private boolean exists;

        @Override
        public boolean existsCategoryByName(String categoryName) {
            return exists;
        }
    }
}
