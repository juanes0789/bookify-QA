package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.BookingHistory;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetBookingHistoryServiceTest {

    @Test
    void shouldReturnOnlyClientBookingsInOutputPortOrder() {
        UUID clientId = UUID.randomUUID();
        List<BookingHistory> expectedBookings = List.of(
                new BookingHistory("Corte de cabello", "Barberia XYZ", LocalDate.parse("2026-09-17"), LocalTime.parse("11:00"), "CONFIRMADA"),
                new BookingHistory("Manicure", "Nails ABC", LocalDate.parse("2026-09-16"), LocalTime.parse("09:00"), "CANCELADA")
        );

        BookingHistoryOutputPort outputPort = queriedClientId -> {
            assertThat(queriedClientId).isEqualTo(clientId);
            return expectedBookings;
        };

        List<BookingHistory> result = new GetBookingHistoryService(outputPort).getHistoryByClient(clientId);

        assertThat(result).containsExactlyElementsOf(expectedBookings);
    }

    @Test
    void shouldReturnEmptyHistoryWhenClientHasNoBookings() {
        UUID clientId = UUID.randomUUID();
        BookingHistoryOutputPort outputPort = queriedClientId -> List.of();

        List<BookingHistory> result = new GetBookingHistoryService(outputPort).getHistoryByClient(clientId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectMissingClientId() {
        BookingHistoryOutputPort outputPort = queriedClientId -> List.of();

        assertThatThrownBy(() -> new GetBookingHistoryService(outputPort).getHistoryByClient(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El cliente es requerido para consultar el historial de reservas.");
    }
}
