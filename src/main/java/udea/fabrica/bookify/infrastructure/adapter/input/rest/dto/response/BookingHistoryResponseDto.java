package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
public class BookingHistoryResponseDto {
    private String servicio;
    private String proveedor;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;

    public BookingHistoryResponseDto(String servicio, String proveedor, LocalDate fecha, LocalTime hora, String estado) {
        this.servicio = servicio;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }
}