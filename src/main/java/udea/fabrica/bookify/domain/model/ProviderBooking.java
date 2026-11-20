package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProviderBooking(
        LocalDate fecha,
        LocalTime hora,
        String cliente,
        String servicio,
        String recurso,
        String estado) { }