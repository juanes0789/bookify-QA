package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookingHistoryListResponseDto {
    private String message;
    private List<BookingHistoryResponseDto> reservas;
}
