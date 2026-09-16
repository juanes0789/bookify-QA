package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.BookingHistory;

import java.util.List;
import java.util.UUID;

public interface BookingHistoryOutputPort {
    List<BookingHistory> findByClientIdOrderByDateDesc(UUID clienteId);
}
