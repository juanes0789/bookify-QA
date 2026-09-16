package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.BookingHistory;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.BookingHistoryListResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.BookingHistoryResponseDto;

import java.util.List;

public class BookingHistoryRestMapper {

    private static final String EMPTY_HISTORY_MESSAGE = "No tienes reservas registradas";

    public static BookingHistoryListResponseDto toResponse(List<BookingHistory> bookings) {
        List<BookingHistoryResponseDto> response = bookings.stream()
                .map(BookingHistoryRestMapper::toResponse)
                .toList();

        return BookingHistoryListResponseDto.builder()
                .message(response.isEmpty() ? EMPTY_HISTORY_MESSAGE : null)
                .reservas(response)
                .build();
    }

    private static BookingHistoryResponseDto toResponse(BookingHistory booking) {
        return BookingHistoryResponseDto.builder()
                .servicio(booking.getService())
                .proveedor(booking.getProvider())
                .fecha(booking.getFecha())
                .hora(booking.getHora())
                .estado(booking.getEstado())
                .build();
    }
}
