package udea.fabrica.bookify.domain.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import udea.fabrica.bookify.domain.model.ProviderBooking;

public interface ProviderBookingQueryPort {
    List<ProviderBooking> findByProviderAndDateRange(UUID providerId, LocalDate from, LocalDate to);
}