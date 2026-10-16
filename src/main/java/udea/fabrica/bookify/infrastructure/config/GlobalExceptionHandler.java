package udea.fabrica.bookify.infrastructure.config;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.BookingNotFoundException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.exception.DoubleBookingException;
import udea.fabrica.bookify.domain.exception.InvalidCancellationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AvailabilityOverlapException.class)
    public ResponseEntity<Map<String, String>> handleOverlapException(AvailabilityOverlapException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DoubleBookingException.class)
    public ResponseEntity<Map<String, String>> doubleBooking(DoubleBookingException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidCancellationException.class, BookingNotFoundException.class})
    public ResponseEntity<Map<String, String>> bookingException(RuntimeException ex) {
        HttpStatus status = ex instanceof BookingNotFoundException
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return build(status, ex.getMessage());
    }

    @ExceptionHandler(DomainForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(DomainForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DomainConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DomainConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}