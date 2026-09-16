package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.BookingHistory;

import java.util.List;
import java.util.UUID;

public interface GetBookingHistoryInputPort {
    List<BookingHistory> getHistoryByClient(UUID clienteId);
}
