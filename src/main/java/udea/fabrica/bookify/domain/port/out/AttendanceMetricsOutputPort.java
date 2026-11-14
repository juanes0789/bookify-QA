package udea.fabrica.bookify.domain.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.AttendancePeriodSummary;

public interface AttendanceMetricsOutputPort {
    AttendancePeriodSummary getPeriodSummary(UUID providerId, OffsetDateTime startDateTime,
                                             OffsetDateTime endDateTime, OffsetDateTime now);
}
