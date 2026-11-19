package udea.fabrica.bookify.domain.model;

import java.util.List;

/** Raw agenda windows deliberately kept independent from persistence and SQL. */
public record OccupancySourceData(List<OccupancySlot> availableSlots, List<OccupancySlot> blockedSlots,
                                  List<OccupancySlot> bookedSlots) {
}
