package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.AttendanceMetrics;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AttendanceMetricsResponseDto;

public final class AttendanceMetricsRestMapper {
    private AttendanceMetricsRestMapper() {
    }

    public static AttendanceMetricsResponseDto toDto(AttendanceMetrics metrics) {
        return new AttendanceMetricsResponseDto(metrics.providerId(), metrics.fechaInicio(), metrics.fechaFin(),
                metrics.citasAtendidas(), metrics.citasNoAsistidas(), metrics.totalClasificadas(),
                metrics.tasaInasistencia(), metrics.pendientesClasificacion(), metrics.mensaje());
    }
}
