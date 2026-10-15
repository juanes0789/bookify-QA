package udea.fabrica.bookify.domain.exception;

public class NonWorkingDayConflictException extends RuntimeException {
    public NonWorkingDayConflictException(String message) {
        super(message);
    }
}
