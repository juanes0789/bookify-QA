package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendanceResponseDto(
        UUID reservaId,
        String estado,
        OffsetDateTime fechaActualizacion,
        String mensaje
) {}

