package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AvailabilityControllerTest {

    @Test
    void shouldCreateAvailabilityAndReturnCreatedResponse() {
        // Arrange
        CreateAvailabilityInputPort inputPort = mock(CreateAvailabilityInputPort.class);
        AvailabilityController controller = new AvailabilityController(inputPort);
        UUID agendaId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-10-01T09:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-10-01T10:00:00Z");

        AvailabilityRequestDto request = new AvailabilityRequestDto();
        request.setAgendaServicioId(agendaId);
        request.setInicioAt(start);
        request.setFinAt(end);
        request.setCreadoPorUsuarioId(userId);

        Availability created = new Availability();
        created.setId(UUID.randomUUID());
        created.setAgendaServicioId(agendaId);
        created.setEstadoDisponibilidadId(1);
        created.setInicioAt(start);
        created.setFinAt(end);
        created.setCreadoPorUsuarioId(userId);
        created.setFechaCreacion(OffsetDateTime.parse("2026-10-01T08:00:00Z"));
        created.setFechaActualizacion(OffsetDateTime.parse("2026-10-01T08:00:00Z"));
        when(inputPort.create(any(Availability.class))).thenReturn(created);

        // Act
        var response = controller.createAvailability(request);

        // Assert
        ArgumentCaptor<Availability> captor = ArgumentCaptor.forClass(Availability.class);
        verify(inputPort).create(captor.capture());
        assertThat(captor.getValue().getAgendaServicioId()).isEqualTo(agendaId);
        assertThat(captor.getValue().getInicioAt()).isEqualTo(start);
        assertThat(captor.getValue().getFinAt()).isEqualTo(end);
        assertThat(captor.getValue().getCreadoPorUsuarioId()).isEqualTo(userId);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getEstadoDisponibilidadId()).isEqualTo(1);
    }
}
