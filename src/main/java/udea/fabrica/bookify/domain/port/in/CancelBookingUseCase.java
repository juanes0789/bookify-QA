package udea.fabrica.bookify.domain.port.in; 
import java.util.UUID; 
import udea.fabrica.bookify.domain.model.Booking;
public interface CancelBookingUseCase { 
    Booking cancel(UUID bookingId); 
}
