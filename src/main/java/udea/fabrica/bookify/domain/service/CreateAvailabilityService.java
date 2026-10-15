package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.NonWorkingDayConflictException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class CreateAvailabilityService implements CreateAvailabilityInputPort {

    private static final String AVAILABLE_STATUS_CODE = "DISPONIBLE";
    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");

    private final AvailabilityOutputPort availabilityOutputPort;
    private final NonWorkingDayOutputPort nonWorkingDayOutputPort;

    public CreateAvailabilityService(AvailabilityOutputPort availabilityOutputPort,
                                     NonWorkingDayOutputPort nonWorkingDayOutputPort) {
        this.availabilityOutputPort = availabilityOutputPort;
        this.nonWorkingDayOutputPort = nonWorkingDayOutputPort;
    }

    @Override
    public Availability create(Availability availability) {
        if (!availability.getInicioAt().isBefore(availability.getFinAt())) {
            throw new IllegalArgumentException("La fecha/hora de inicio debe ser estrictamente anterior a la fecha/hora fin.");
        }

        if (!availabilityOutputPort.existsActiveAgendaServicio(availability.getAgendaServicioId())) {
            throw new IllegalArgumentException("La agenda de servicio no existe o no está activa.");
        }

        // Validar día no laborable antes de verificar solapamiento
        UUID proveedorId = nonWorkingDayOutputPort.findProviderIdByAgendaServicioId(availability.getAgendaServicioId());
        if (proveedorId != null) {
            LocalDate fechaInicio = availability.getInicioAt()
                    .atZoneSameInstant(BOGOTA_ZONE)
                    .toLocalDate();
            if (nonWorkingDayOutputPort.isNonWorkingDay(proveedorId, fechaInicio)) {
                throw new NonWorkingDayConflictException("La fecha está marcada como no laborable");
            }
        }

        boolean hasOverlap = availabilityOutputPort.existsOverlapping(
                availability.getAgendaServicioId(),
                availability.getInicioAt(),
                availability.getFinAt()
        );

        if (hasOverlap) {
            throw new AvailabilityOverlapException("El rango de horario ingresado se traslapa con un horario ya registrado.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        availability.setFechaCreacion(now);
        availability.setFechaActualizacion(now);

        Integer availableStatusId = availabilityOutputPort.findAvailabilityStatusIdByCode(AVAILABLE_STATUS_CODE);
        if (availableStatusId == null) {
            throw new IllegalStateException("No existe el estado de disponibilidad 'DISPONIBLE' en la base de datos.");
        }
        availability.setEstadoDisponibilidadId(availableStatusId);

        return availabilityOutputPort.save(availability);
    }
}