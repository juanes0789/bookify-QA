package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import udea.fabrica.bookify.domain.exception.InvalidSearchCriteriaException;
import udea.fabrica.bookify.domain.model.AvailableScheduleWithResource;
import udea.fabrica.bookify.domain.model.SearchServicesResult;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.domain.port.in.SearchServicesInputPort;
import udea.fabrica.bookify.infrastructure.config.GlobalExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchServicesControllerTest {

    private static final String ENDPOINT = "/api/v1/catalog/services/search";

    private MockMvc mockMvc;
    private SearchServicesInputPort searchServicesInputPort;

    @BeforeEach
    void setUp() {
        searchServicesInputPort = mock(SearchServicesInputPort.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SearchServicesController(searchServicesInputPort))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnEnrichedServiceSearchResult() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-10-15T10:00:00Z");
        ServiceSearchItem item = new ServiceSearchItem();
        item.setServiceId(serviceId);
        item.setService("Corte de cabello");
        item.setProvider("Barbería Central");
        item.setCategory("Barbería y belleza");
        item.addAvailableSchedule(new AvailableScheduleWithResource(availabilityId, start, start.plusMinutes(30), "Barbero Carlos"));
        when(searchServicesInputPort.search("CORTE", null))
                .thenReturn(new SearchServicesResult(List.of(item), null));

        mockMvc.perform(get(ENDPOINT).param("texto", "CORTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicios[0].serviceId").value(serviceId.toString()))
                .andExpect(jsonPath("$.servicios[0].service").value("Corte de cabello"))
                .andExpect(jsonPath("$.servicios[0].provider").value("Barbería Central"))
                .andExpect(jsonPath("$.servicios[0].category").value("Barbería y belleza"))
                .andExpect(jsonPath("$.servicios[0].availableSchedules[0].availabilityId").value(availabilityId.toString()))
                .andExpect(jsonPath("$.servicios[0].availableSchedules[0].recurso").value("Barbero Carlos"));
    }

    @Test
    void shouldReturnEmptyResultMessage() throws Exception {
        when(searchServicesInputPort.search("yoga", null))
                .thenReturn(new SearchServicesResult(List.of(), "No se encontraron servicios"));

        mockMvc.perform(get(ENDPOINT).param("texto", "yoga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicios").isEmpty())
                .andExpect(jsonPath("$.mensaje").value("No se encontraron servicios"));
    }

    @Test
    void shouldReturnBadRequestWhenNoCriteriaAreProvided() throws Exception {
        when(searchServicesInputPort.search(null, null))
                .thenThrow(new InvalidSearchCriteriaException("Debe ingresar al menos un criterio de búsqueda: texto o categoría"));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Debe ingresar al menos un criterio de búsqueda: texto o categoría"));
    }
}
