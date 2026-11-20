package udea.fabrica.bookify.domain.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.Availability;

public interface ProviderAvailabilityPort {

    /** Bloquea el horario y devuelve true solo si pertenece al proveedor. */
    boolean lockOwnedAvailability(UUID availabilityId, UUID providerId);

    int countConfirmedBookings(UUID availabilityId);

    boolean existsOverlappingExcluding(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt);

    Availability updateRange(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt);

    void delete(UUID availabilityId);
}