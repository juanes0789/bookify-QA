package udea.fabrica.bookify.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.ClientNoShowReport;

public interface GetClientNoShowReportUseCase {
    ClientNoShowReport getReport(UUID authenticatedUserId, LocalDate fechaInicio,
                                 LocalDate fechaFin, Integer limite);
}
