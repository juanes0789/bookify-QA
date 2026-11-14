package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Proyección ligera usada por la validación de asistencia.
 * Transporta sólo los campos necesarios para verificar pertenencia, estado y tiempo.
 */
public record BookingAttendanceInfo(
        UUID id,
        UUID providerId,
        String estadoCodigo,
        Integer estadoId,
        OffsetDateTime finAt
) {}

