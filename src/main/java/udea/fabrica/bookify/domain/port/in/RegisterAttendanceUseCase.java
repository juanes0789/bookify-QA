package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.model.RegisterAttendanceCommand;

public interface RegisterAttendanceUseCase {
    Booking registerAttendance(RegisterAttendanceCommand command);
}

