package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import java.time.Instant;
import java.util.UUID;

public interface SearchServicesProjection {
    UUID getServiceId();
    String getService();
    String getProvider();
    String getCategory();
    UUID getAvailabilityId();
    Instant getInicioAt();
    Instant getFinAt();
    String getResourceName();
}
