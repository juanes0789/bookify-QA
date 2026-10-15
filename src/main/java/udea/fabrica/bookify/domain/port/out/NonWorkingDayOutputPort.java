package udea.fabrica.bookify.domain.port.out;

import udea.fabrica.bookify.domain.model.NonWorkingDay;

import java.time.LocalDate;
import java.util.UUID;

public interface NonWorkingDayOutputPort {
    NonWorkingDay save(NonWorkingDay nonWorkingDay);
    boolean existsByProveedorIdAndFecha(UUID proveedorId, LocalDate fecha);
    boolean isNonWorkingDay(UUID proveedorId, LocalDate fecha);
    UUID findProviderIdByAgendaServicioId(UUID agendaServicioId);
}
