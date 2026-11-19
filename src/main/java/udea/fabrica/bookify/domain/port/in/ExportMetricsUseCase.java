package udea.fabrica.bookify.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface ExportMetricsUseCase {
    String export(UUID authenticatedUserId, LocalDate fechaInicio, LocalDate fechaFin);
}
