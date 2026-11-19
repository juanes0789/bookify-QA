package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;

/** A unit of agenda capacity. One slot exists for every resource that can serve it. */
public record OccupancySlot(OffsetDateTime startAt, OffsetDateTime endAt) {
}
