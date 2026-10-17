package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class BookingResponseDto {
    private UUID id;
    private UUID clienteId;
    private UUID disponibilidadId;
    private Integer estadoReservaId;
    private OffsetDateTime fechaReserva;
    private String notas;

    public BookingResponseDto(UUID id, UUID clienteId, UUID disponibilidadId, Integer estadoReservaId, OffsetDateTime fechaReserva, String notas) {
        this.id = id;
        this.clienteId = clienteId;
        this.disponibilidadId = disponibilidadId;
        this.estadoReservaId = estadoReservaId;
        this.fechaReserva = fechaReserva;
        this.notas = notas;
    }
}