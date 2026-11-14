package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AttendanceRequestDto(

        @NotNull(message = "El estado es obligatorio")
        @Pattern(regexp = "^(ATENDIDA|NO_ASISTIO)$", message = "El estado debe ser 'ATENDIDA' o 'NO_ASISTIO'")
        String estado,

        @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
        String motivo
) {}

