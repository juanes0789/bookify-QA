package udea.fabrica.bookify.domain.model;

import java.util.UUID;

public record RegisterAttendanceCommand(
        UUID bookingId,
        UUID authenticatedUserId,
        AttendanceStatus status,
        String motivo
) {}

