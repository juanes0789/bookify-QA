package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAvailabilityRequestDto {
    @NotNull(message = "La hora de inicio es obligatoria")
    private OffsetDateTime inicioAt;

    @NotNull(message = "La hora final es obligatoria")
    private OffsetDateTime finAt;
}