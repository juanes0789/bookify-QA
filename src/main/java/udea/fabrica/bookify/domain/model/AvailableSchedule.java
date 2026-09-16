package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AvailableSchedule {
    private UUID availabilityId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;

    public AvailableSchedule() {
    }

    public AvailableSchedule(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt) {
        this.availabilityId = availabilityId;
        this.inicioAt = inicioAt;
        this.finAt = finAt;
    }

    public UUID getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(UUID availabilityId) {
        this.availabilityId = availabilityId;
    }

    public OffsetDateTime getInicioAt() {
        return inicioAt;
    }

    public void setInicioAt(OffsetDateTime inicioAt) {
        this.inicioAt = inicioAt;
    }

    public OffsetDateTime getFinAt() {
        return finAt;
    }

    public void setFinAt(OffsetDateTime finAt) {
        this.finAt = finAt;
    }
}
