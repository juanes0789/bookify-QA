package udea.fabrica.bookify.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceMetrics(UUID providerId, LocalDate fechaInicio, LocalDate fechaFin,
                                long citasAtendidas, long citasNoAsistidas, long totalClasificadas,
                                BigDecimal tasaInasistencia, long pendientesClasificacion, String mensaje) {
}
