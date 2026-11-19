package udea.fabrica.bookify.domain.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.OccupancySourceData;

public interface OccupancyMetricsOutputPort {
    OccupancySourceData getSourceData(UUID providerId, OffsetDateTime start, OffsetDateTime end);
}
