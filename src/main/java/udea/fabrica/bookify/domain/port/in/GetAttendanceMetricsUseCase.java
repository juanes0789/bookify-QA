package udea.fabrica.bookify.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.AttendanceMetrics;

public interface GetAttendanceMetricsUseCase {
    AttendanceMetrics getMetrics(UUID authenticatedUserId, LocalDate fechaInicio, LocalDate fechaFin);
}
