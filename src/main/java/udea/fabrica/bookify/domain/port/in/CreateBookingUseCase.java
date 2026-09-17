package udea.fabrica.bookify.domain.port.in; 
import udea.fabrica.bookify.domain.model.Booking;
public interface CreateBookingUseCase { 
    Booking create(Booking booking); 
}
