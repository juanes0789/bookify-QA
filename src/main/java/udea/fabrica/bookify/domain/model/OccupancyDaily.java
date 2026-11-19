package udea.fabrica.bookify.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OccupancyDaily(LocalDate fecha, long minutosDisponibles, long minutosBloqueados,
                             long minutosOcupados, BigDecimal porcentajeOcupacion) {
}
