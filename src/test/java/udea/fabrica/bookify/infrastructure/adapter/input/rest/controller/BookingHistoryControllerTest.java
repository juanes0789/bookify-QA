package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.BookingHistory;
import udea.fabrica.bookify.domain.port.in.GetBookingHistoryInputPort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingHistoryControllerTest {

    @Test
    void shouldReturnBookingHistoryForClient() {
        // Arrange
        GetBookingHistoryInputPort inputPort = mock(GetBookingHistoryInputPort.class);
        BookingHistoryController controller = new BookingHistoryController(inputPort);
        UUID clientId = UUID.randomUUID();
        when(inputPort.getHistoryByClient(clientId)).thenReturn(List.of(
                new BookingHistory("Corte", "Barberia", LocalDate.parse("2026-10-01"), LocalTime.parse("09:00"), "CONFIRMADA")
        ));

        // Act
        var response = controller.getBookingHistory(clientId);

        // Assert
        verify(inputPort).getHistoryByClient(clientId);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isNull();
        assertThat(response.getBody().getReservas()).hasSize(1);
        assertThat(response.getBody().getReservas().get(0).getServicio()).isEqualTo("Corte");
    }
}
