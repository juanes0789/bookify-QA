package udea.fabrica.bookify.domain.exception;

public class ReservationNotEndedException extends DomainConflictException {
    public ReservationNotEndedException() {
        super("No se puede registrar asistencia de una reserva que todavía no ha finalizado.");
    }
}

