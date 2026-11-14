package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceMetricsResponseDto(UUID proveedorId, LocalDate fechaInicio, LocalDate fechaFin,
                                           long citasAtendidas, long citasNoAsistidas,
                                           long totalClasificadas, BigDecimal tasaInasistencia,
                                           long pendientesClasificacion, String mensaje) {
}
