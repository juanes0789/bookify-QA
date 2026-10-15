package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.exception.NonWorkingDayConflictException;
import udea.fabrica.bookify.domain.exception.PastDateException;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.in.RegisterNonWorkingDayInputPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class NonWorkingDayService implements RegisterNonWorkingDayInputPort {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");

    private final NonWorkingDayOutputPort nonWorkingDayOutputPort;

    public NonWorkingDayService(NonWorkingDayOutputPort nonWorkingDayOutputPort) {
        this.nonWorkingDayOutputPort = nonWorkingDayOutputPort;
    }

    @Override
    public NonWorkingDay register(UUID proveedorId, LocalDate fecha, String motivo) {
        // Regla 1: Validar longitud del motivo
        if (motivo != null && motivo.trim().length() > 100) {
            throw new IllegalArgumentException("El motivo no puede exceder 100 caracteres");
        }

        // Regla 2: Validar que la fecha no sea pasada
        LocalDate hoy = LocalDate.now(BOGOTA_ZONE);
        if (fecha.isBefore(hoy)) {
            throw new PastDateException("La fecha debe ser igual o posterior a hoy");
        }

        // Regla 3: Validar unicidad
        if (nonWorkingDayOutputPort.existsByProveedorIdAndFecha(proveedorId, fecha)) {
            throw new NonWorkingDayConflictException("La fecha ya está registrada como no laborable");
        }

        // Regla 4: Guardar
        NonWorkingDay nonWorkingDay = new NonWorkingDay();
        nonWorkingDay.setProveedorId(proveedorId);
        nonWorkingDay.setFecha(fecha);
        nonWorkingDay.setMotivo(motivo);
        nonWorkingDay.setFechaCreacion(OffsetDateTime.now(BOGOTA_ZONE));

        return nonWorkingDayOutputPort.save(nonWorkingDay);
    }
}
