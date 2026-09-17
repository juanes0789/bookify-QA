package udea.fabrica.bookify.domain.port.out;
import java.util.*; 
import udea.fabrica.bookify.domain.model.*;
public interface BookingRepositoryPort {
    Booking save(Booking b); 
    Optional<Booking> findById(UUID id); 
    Optional<UUID> findAvailabilityIdByBookingId(UUID id);
    void createOccupation(UUID availabilityId, UUID bookingId); 
    void deleteOccupation(UUID availabilityId, UUID bookingId);
    void saveStatusHistory(BookingStatusHistory h); 
    Integer findReservationStatusIdByCode(String code);
}
