package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.in.ManageAvailabilityUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.UpdateAvailabilityRequestDto;

class AvailabilityControllerTest {

    private final CreateAvailabilityInputPort inputPort = mock(CreateAvailabilityInputPort.class);
    private final ManageAvailabilityUseCase manageUseCase = mock(ManageAvailabilityUseCase.class);
    private final AvailabilityController controller = new AvailabilityController(inputPort, manageUseCase);

    @Test
    void shouldCreateAvailabilityAndReturnCreatedResponse() {
        // Arrange
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

    @Test
    void shouldUpdateAvailabilityWithAuthenticatedUser_andReturnOk() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-11-25T10:00:00-05:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-11-25T11:00:00-05:00");
        var authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), "token", java.util.List.of(new SimpleGrantedAuthority("PROVEEDOR")));

        UpdateAvailabilityRequestDto request = new UpdateAvailabilityRequestDto();
        request.setInicioAt(start);
        request.setFinAt(end);

        Availability updated = new Availability(availabilityId, UUID.randomUUID(), 1, start, end, null,
                start, start);
        when(manageUseCase.update(userId, "PROVEEDOR", availabilityId, start, end)).thenReturn(updated);

        // Act
        var response = controller.updateAvailability(availabilityId, request, authentication);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(availabilityId);
        assertThat(response.getBody().getInicioAt()).isEqualTo(start);
        assertThat(response.getBody().getFinAt()).isEqualTo(end);
    }

    @Test
    void shouldDeleteAvailabilityWithAuthenticatedUser_andReturnNoContent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), "token", java.util.List.of(new SimpleGrantedAuthority("PROVEEDOR")));

        // Act
        var response = controller.deleteAvailability(availabilityId, authentication);

        // Assert
        verify(manageUseCase).delete(userId, "PROVEEDOR", availabilityId);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
}