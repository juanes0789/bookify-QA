package udea.fabrica.bookify.domain.exception;

public class DomainForbiddenException extends RuntimeException {
    public DomainForbiddenException(String message) {
        super(message);
    }
}