package udea.fabrica.bookify.domain.service;

import java.time.LocalDate;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.AttendanceMetrics;
import udea.fabrica.bookify.domain.model.OccupancyDaily;
import udea.fabrica.bookify.domain.model.OccupancyMetrics;
import udea.fabrica.bookify.domain.exception.InsufficientDataException;
import udea.fabrica.bookify.domain.port.in.ExportMetricsUseCase;
import udea.fabrica.bookify.domain.port.in.GetAttendanceMetricsUseCase;
import udea.fabrica.bookify.domain.port.in.GetOccupancyMetricsUseCase;

public class MetricsCsvExportService implements ExportMetricsUseCase {
    private final GetOccupancyMetricsUseCase occupancy;
    private final GetAttendanceMetricsUseCase attendance;
    public MetricsCsvExportService(GetOccupancyMetricsUseCase occupancy, GetAttendanceMetricsUseCase attendance) {
        this.occupancy = occupancy; this.attendance = attendance;
    }
    @Override public String export(UUID userId, LocalDate from, LocalDate to) {
        OccupancyMetrics o = occupancy.getMetrics(userId, from, to);
        AttendanceMetrics a = attendance.getMetrics(userId, from, to);
        if (o.mensaje() != null || a.mensaje() != null) throw new InsufficientDataException("Sin datos suficientes");
        StringBuilder csv = new StringBuilder("Periodo inicio,Periodo fin,Indicador,Valor\n");
        row(csv, from, to, "Ocupacion (%)", o.porcentajeOcupacion());
        row(csv, from, to, "Inasistencia (%)", a.tasaInasistencia());
        csv.append("Fecha,Minutos disponibles,Minutos bloqueados,Minutos ocupados,Ocupacion (%)\n");
        for (OccupancyDaily d : o.diario()) csv.append(cell(d.fecha())).append(',').append(d.minutosDisponibles()).append(',')
                .append(d.minutosBloqueados()).append(',').append(d.minutosOcupados()).append(',').append(cell(d.porcentajeOcupacion())).append('\n');
        return csv.toString();
    }
    private void row(StringBuilder csv, Object from, Object to, String name, Object value) {
        csv.append(cell(from)).append(',').append(cell(to)).append(',').append(cell(name)).append(',').append(cell(value)).append('\n');
    }
    /** Quotes values and prefixes spreadsheet formula starters. */
    private String cell(Object value) {
        String text = value == null ? "" : value.toString();
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) text = "'" + text;
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}
