package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OccupancyMetricsResponseDto(LocalDate fechaInicio, LocalDate fechaFin, long minutosDisponibles,
        long minutosBloqueados, long minutosOcupados, BigDecimal porcentajeOcupacion,
        List<OccupancyDailyResponseDto> diario, String mensaje) { }
