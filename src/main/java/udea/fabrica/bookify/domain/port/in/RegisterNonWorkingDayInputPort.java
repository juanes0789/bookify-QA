package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.NonWorkingDay;

import java.time.LocalDate;
import java.util.UUID;

public interface RegisterNonWorkingDayInputPort {
    NonWorkingDay register(UUID proveedorId, LocalDate fecha, String motivo);
}
