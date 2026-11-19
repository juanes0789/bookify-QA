package udea.fabrica.bookify.domain.port.out;

import java.util.UUID;

/** Secondary action: failures must never alter the confirmed booking. */
public interface BookingConfirmationNotificationPort {
    void sendOnce(UUID bookingId);
}
