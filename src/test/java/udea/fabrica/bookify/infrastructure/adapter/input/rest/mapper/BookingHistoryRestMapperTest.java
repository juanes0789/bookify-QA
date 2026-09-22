package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.BookingHistory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingHistoryRestMapperTest {

    @Test
    void shouldReturnEmptyHistoryMessageWhenListIsEmpty() {
        // Arrange
        List<BookingHistory> bookings = List.of();

        // Act
        var result = BookingHistoryRestMapper.toResponse(bookings);

        // Assert
        assertThat(result.getReservas()).isEmpty();
        assertThat(result.getMessage()).isEqualTo("No tienes reservas registradas");
    }

    @Test
    void shouldMapBookingHistoryRowsToResponseDtos() {
        // Arrange
        List<BookingHistory> bookings = List.of(
                new BookingHistory("Masaje", "Spa", LocalDate.parse("2026-10-05"), LocalTime.parse("15:30"), "CONFIRMADA")
        );

        // Act
        var result = BookingHistoryRestMapper.toResponse(bookings);

        // Assert
        assertThat(result.getMessage()).isNull();
        assertThat(result.getReservas()).hasSize(1);
        assertThat(result.getReservas().get(0).getServicio()).isEqualTo("Masaje");
        assertThat(result.getReservas().get(0).getProveedor()).isEqualTo("Spa");
    }
}
