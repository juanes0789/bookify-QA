package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.model.BookingHistory;
import udea.fabrica.bookify.domain.port.in.GetBookingHistoryInputPort;
import udea.fabrica.bookify.domain.port.out.BookingHistoryOutputPort;

import java.util.List;
import java.util.UUID;

public class GetBookingHistoryService implements GetBookingHistoryInputPort {

    private final BookingHistoryOutputPort bookingHistoryOutputPort;

    public GetBookingHistoryService(BookingHistoryOutputPort bookingHistoryOutputPort) {
        this.bookingHistoryOutputPort = bookingHistoryOutputPort;
    }

    @Override
    public List<BookingHistory> getHistoryByClient(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El cliente es requerido para consultar el historial de reservas.");
        }

        return bookingHistoryOutputPort.findByClientIdOrderByDateDesc(clienteId);
    }
}
