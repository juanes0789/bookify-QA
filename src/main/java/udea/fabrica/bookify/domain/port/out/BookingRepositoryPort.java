package udea.fabrica.bookify.domain.port.out;
import java.time.OffsetDateTime;
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
    Optional<BookingAttendanceInfo> findAttendanceInfoById(UUID bookingId);
    void updateBookingStatus(UUID bookingId, Integer statusId, OffsetDateTime updatedAt);
}
