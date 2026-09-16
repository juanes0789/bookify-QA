package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class BookingHistoryResponseDto {
    private String servicio;
    private String proveedor;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
}
