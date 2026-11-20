package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProviderBookingListResponseDto(int total, String mensaje, List<Item> reservas) {

    public record Item(LocalDate fecha, LocalTime hora, String cliente,
                       String servicio, String recurso, String estado) { }
}