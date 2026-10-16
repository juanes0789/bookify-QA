package udea.fabrica.bookify.domain.exception;

public class PastDateException extends RuntimeException {
    public PastDateException(String message) {
        super(message);
    }
}
