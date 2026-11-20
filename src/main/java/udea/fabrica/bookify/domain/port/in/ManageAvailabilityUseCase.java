package udea.fabrica.bookify.domain.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.Availability;

public interface ManageAvailabilityUseCase {
    Availability update(UUID userId, String userRole, UUID availabilityId,
                        OffsetDateTime inicioAt, OffsetDateTime finAt);

    void delete(UUID userId, String userRole, UUID availabilityId);
}