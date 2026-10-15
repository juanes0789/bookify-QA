package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AvailableScheduleWithResource {
    private UUID availabilityId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;
    private String recurso;

    public AvailableScheduleWithResource(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt, String recurso) {
        this.availabilityId = availabilityId;
        this.inicioAt = inicioAt;
        this.finAt = finAt;
        this.recurso = recurso;
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

    public String getRecurso() {
        return recurso;
    }
}
