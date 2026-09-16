package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import java.time.Instant;
import java.util.UUID;

public interface ServiceCatalogProjection {
    UUID getServiceId();
    String getService();
    String getProvider();
    UUID getAvailabilityId();
    Instant getInicioAt();
    Instant getFinAt();
}