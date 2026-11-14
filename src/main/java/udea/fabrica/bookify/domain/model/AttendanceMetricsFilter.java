package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceMetricsFilter(UUID providerId, LocalDate fechaInicio, LocalDate fechaFin) {
    public AttendanceMetricsFilter {
        if (providerId == null) {
            throw new IllegalArgumentException("El proveedor es requerido.");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
