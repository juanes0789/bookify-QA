package udea.fabrica.bookify.domain.port.out;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ResourceAvailabilityPort {
    Optional<UUID> findAvailableResource(UUID providerId, OffsetDateTime startAt, OffsetDateTime endAt);
    void assignResourceToBooking(UUID bookingId, UUID resourceId);
}
