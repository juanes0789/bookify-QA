package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
public class BookingHistoryListResponseDto {
    private String message;
    private List<BookingHistoryResponseDto> reservas;

    public BookingHistoryListResponseDto(String message, List<BookingHistoryResponseDto> reservas) {
        this.message = message;
        this.reservas = reservas;
    }
}