package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class AvailabilityResponseDto {
    private UUID id;
    private UUID agendaServicioId;
    private Integer estadoDisponibilidadId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;
    private UUID creadoPorUsuarioId;
    private OffsetDateTime fechaCreacion;
}