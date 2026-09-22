package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.port.in.CancelBookingUseCase;
import udea.fabrica.bookify.domain.port.in.CreateBookingUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.BookingRequestDto;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Test
    void shouldCreateBookingAndReturnCreatedResponse() {
        // Arrange
        CreateBookingUseCase createUseCase = mock(CreateBookingUseCase.class);
        CancelBookingUseCase cancelUseCase = mock(CancelBookingUseCase.class);
        BookingController controller = new BookingController(createUseCase, cancelUseCase);
        UUID clientId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();

        BookingRequestDto request = new BookingRequestDto();
        request.setClienteId(clientId);
        request.setDisponibilidadId(availabilityId);
        request.setNotas("Primera cita");

        Booking created = new Booking();
        created.setId(UUID.randomUUID());
        created.setClienteId(clientId);
        created.setAvailabilityId(availabilityId);
        created.setEstadoReservaId(10);
        created.setFechaReserva(OffsetDateTime.parse("2026-10-02T10:00:00Z"));
        created.setNotas("Primera cita");
        when(createUseCase.create(any(Booking.class))).thenReturn(created);

        // Act
        var response = controller.create(request);

        // Assert
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(createUseCase).create(captor.capture());
        assertThat(captor.getValue().getClienteId()).isEqualTo(clientId);
        assertThat(captor.getValue().getAvailabilityId()).isEqualTo(availabilityId);
        assertThat(captor.getValue().getNotas()).isEqualTo("Primera cita");
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getEstadoReservaId()).isEqualTo(10);
    }

    @Test
    void shouldCancelBookingAndReturnResponseDto() {
        // Arrange
        CreateBookingUseCase createUseCase = mock(CreateBookingUseCase.class);
        CancelBookingUseCase cancelUseCase = mock(CancelBookingUseCase.class);
        BookingController controller = new BookingController(createUseCase, cancelUseCase);
        UUID bookingId = UUID.randomUUID();

        Booking cancelled = new Booking();
        cancelled.setId(bookingId);
        cancelled.setClienteId(UUID.randomUUID());
        cancelled.setAvailabilityId(UUID.randomUUID());
        cancelled.setEstadoReservaId(11);
        when(cancelUseCase.cancel(bookingId)).thenReturn(cancelled);

        // Act
        var response = controller.cancel(bookingId);

        // Assert
        verify(cancelUseCase).cancel(bookingId);
        assertThat(response.getId()).isEqualTo(bookingId);
        assertThat(response.getEstadoReservaId()).isEqualTo(11);
    }
}
