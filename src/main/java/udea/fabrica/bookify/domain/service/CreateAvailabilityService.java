package udea.fabrica.bookify.domain.service;

import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.in.CreateAvailabilityInputPort;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;

import java.time.OffsetDateTime;

public class CreateAvailabilityService implements CreateAvailabilityInputPort {

    private static final String AVAILABLE_STATUS_CODE = "DISPONIBLE";

    private final AvailabilityOutputPort availabilityOutputPort;

    public CreateAvailabilityService(AvailabilityOutputPort availabilityOutputPort) {
        this.availabilityOutputPort = availabilityOutputPort;
    }

    @Override
    public Availability create(Availability availability) {
        if (!availability.getInicioAt().isBefore(availability.getFinAt())) {
            throw new IllegalArgumentException("La fecha/hora de inicio debe ser estrictamente anterior a la fecha/hora fin.");
        }

        if (!availabilityOutputPort.existsActiveAgendaServicio(availability.getAgendaServicioId())) {
            throw new IllegalArgumentException("La agenda de servicio no existe o no está activa.");
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