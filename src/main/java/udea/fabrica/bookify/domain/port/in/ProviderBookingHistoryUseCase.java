package udea.fabrica.bookify.domain.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.ProviderBooking;

public interface ProviderBookingHistoryUseCase {
    List<ProviderBooking> getHistory(UUID userId, String userRole, LocalDate from, LocalDate to);

    Optional<String> exportCsv(UUID userId, String userRole, LocalDate from, LocalDate to);
}