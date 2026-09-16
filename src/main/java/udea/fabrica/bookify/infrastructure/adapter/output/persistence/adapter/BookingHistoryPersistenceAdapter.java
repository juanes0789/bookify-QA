package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.BookingHistory;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.BookingHistoryJpaRepository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class BookingHistoryPersistenceAdapter implements BookingHistoryOutputPort {

    private final BookingHistoryJpaRepository bookingHistoryJpaRepository;

    public BookingHistoryPersistenceAdapter(BookingHistoryJpaRepository bookingHistoryJpaRepository) {
        this.bookingHistoryJpaRepository = bookingHistoryJpaRepository;
    }

    @Override
    public List<BookingHistory> findByClientIdOrderByDateDesc(UUID clienteId) {
        return bookingHistoryJpaRepository.findBookingHistoryByClientId(clienteId).stream()
                .map(row -> {
                    var fechaHora = row.getFechaHora().atOffset(ZoneOffset.UTC);
                    return new BookingHistory(
                            row.getService(),
                            row.getProvider(),
                            fechaHora.toLocalDate(),
                            fechaHora.toLocalTime(),
                            row.getEstado()
                    );
                })
                .toList();
    }
}
