package udea.fabrica.bookify.domain.port.in;

import udea.fabrica.bookify.domain.model.Availability;

public interface CreateAvailabilityInputPort {
    Availability create(Availability availability);
}