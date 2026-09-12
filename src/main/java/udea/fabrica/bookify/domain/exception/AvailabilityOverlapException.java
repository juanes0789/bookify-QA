package udea.fabrica.bookify.domain.exception;

public class AvailabilityOverlapException extends RuntimeException {
    public AvailabilityOverlapException(String message) {
        super(message);
    }
}