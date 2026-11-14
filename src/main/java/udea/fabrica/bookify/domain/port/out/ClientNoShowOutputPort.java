package udea.fabrica.bookify.domain.port.out;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.ClientNoShowSummary;

public interface ClientNoShowOutputPort {
    List<ClientNoShowSummary> findClientsWithMostNoShows(UUID providerId,
            OffsetDateTime startDateTime, OffsetDateTime endDateTime, Integer limite);
}
