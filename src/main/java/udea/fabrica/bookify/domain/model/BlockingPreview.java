package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BlockingPreview(UUID providerId, LocalDate date, List<UUID> affectedBookingIds) {
    public int affectedBookingsCount() { return affectedBookingIds.size(); }
}
