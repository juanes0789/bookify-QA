package udea.fabrica.bookify.domain.exception;

public class InvalidReservationStateException extends DomainConflictException {
    public InvalidReservationStateException(String message) {
        super(message);
    }
}

