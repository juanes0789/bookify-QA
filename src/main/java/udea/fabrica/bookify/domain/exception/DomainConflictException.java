package udea.fabrica.bookify.domain.exception;

public class DomainConflictException extends RuntimeException {
    public DomainConflictException(String message) {
        super(message);
    }
}