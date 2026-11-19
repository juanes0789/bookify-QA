package udea.fabrica.bookify.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OccupancyMetrics(UUID providerId, LocalDate fechaInicio, LocalDate fechaFin,
                               long minutosDisponibles, long minutosBloqueados, long minutosOcupados,
                               BigDecimal porcentajeOcupacion, List<OccupancyDaily> diario, String mensaje) {
}
