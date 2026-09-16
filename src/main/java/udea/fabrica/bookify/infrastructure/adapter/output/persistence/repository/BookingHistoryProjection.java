package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import java.time.Instant;

public interface BookingHistoryProjection {
    String getService();
    String getProvider();
    Instant getFechaHora();
    String getEstado();
}
