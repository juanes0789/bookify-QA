package udea.fabrica.bookify.domain.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.NonWorkingDay;

public interface NonWorkingDayPort {
    List<UUID> findAffectedBookingIds(UUID providerId, LocalDate date);
    NonWorkingDay save(NonWorkingDay nonWorkingDay);
}
