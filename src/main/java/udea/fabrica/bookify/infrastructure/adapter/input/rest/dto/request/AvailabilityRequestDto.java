package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AvailabilityRequestDto {
    @NotNull private UUID agendaServicioId;
    @NotNull private OffsetDateTime inicioAt;
    @NotNull private OffsetDateTime finAt;
    private UUID creadoPorUsuarioId;
}