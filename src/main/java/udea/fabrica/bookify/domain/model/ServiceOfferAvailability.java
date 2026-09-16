package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ServiceOfferAvailability {
    private UUID serviceId;
    private String service;
    private String provider;
    private UUID availabilityId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;

    public ServiceOfferAvailability(UUID serviceId, String service, String provider,
                                    UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt) {
        this.serviceId = serviceId;
        this.service = service;
        this.provider = provider;
        this.availabilityId = availabilityId;
        this.inicioAt = inicioAt;
        this.finAt = finAt;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public String getService() {
        return service;
    }

    public String getProvider() {
        return provider;
    }

    public UUID getAvailabilityId() {
        return availabilityId;
    }

    public OffsetDateTime getInicioAt() {
        return inicioAt;
    }

    public OffsetDateTime getFinAt() {
        return finAt;
    }
}
