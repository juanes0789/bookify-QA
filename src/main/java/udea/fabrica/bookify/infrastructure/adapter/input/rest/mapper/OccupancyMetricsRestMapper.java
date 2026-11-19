package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.OccupancyDaily;
import udea.fabrica.bookify.domain.model.OccupancyMetrics;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.OccupancyDailyResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.OccupancyMetricsResponseDto;

public final class OccupancyMetricsRestMapper {
    private OccupancyMetricsRestMapper() { }
    public static OccupancyMetricsResponseDto toDto(OccupancyMetrics metrics) {
        return new OccupancyMetricsResponseDto(metrics.fechaInicio(), metrics.fechaFin(), metrics.minutosDisponibles(),
                metrics.minutosBloqueados(), metrics.minutosOcupados(), metrics.porcentajeOcupacion(),
                metrics.diario().stream().map(OccupancyMetricsRestMapper::daily).toList(), metrics.mensaje());
    }
    private static OccupancyDailyResponseDto daily(OccupancyDaily day) {
        return new OccupancyDailyResponseDto(day.fecha(), day.minutosDisponibles(), day.minutosBloqueados(),
                day.minutosOcupados(), day.porcentajeOcupacion());
    }
}
