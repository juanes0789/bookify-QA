package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class AvailabilityResponseDto {
    private UUID id;
    private UUID agendaServicioId;
    private Integer estadoDisponibilidadId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;
    private UUID creadoPorUsuarioId;
    private OffsetDateTime fechaCreacion;

    public AvailabilityResponseDto(UUID id, UUID agendaServicioId, Integer estadoDisponibilidadId,
                                   OffsetDateTime inicioAt, OffsetDateTime finAt,
                                   UUID creadoPorUsuarioId, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.agendaServicioId = agendaServicioId;
        this.estadoDisponibilidadId = estadoDisponibilidadId;
        this.inicioAt = inicioAt;
        this.finAt = finAt;
        this.creadoPorUsuarioId = creadoPorUsuarioId;
        this.fechaCreacion = fechaCreacion;
    }
}