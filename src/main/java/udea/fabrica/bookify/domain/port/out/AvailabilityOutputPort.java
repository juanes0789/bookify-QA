package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.Availability;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AvailabilityOutputPort {
    Availability save(Availability availability);
    boolean existsOverlapping(UUID agendaServicioId, OffsetDateTime inicioAt, OffsetDateTime finAt);
    boolean existsActiveAgendaServicio(UUID agendaServicioId);
    Integer findAvailabilityStatusIdByCode(String codigo);
}