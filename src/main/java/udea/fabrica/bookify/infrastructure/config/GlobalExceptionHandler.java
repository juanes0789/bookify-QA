package udea.fabrica.bookify.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AvailabilityOverlapException.class)
    public ResponseEntity<Map<String, String>> handleOverlapException(AvailabilityOverlapException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DoubleBookingException.class)
    public ResponseEntity<Map<String, String>> doubleBooking(DoubleBookingException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage())); }
    @ExceptionHandler({InvalidCancellationException.class, BookingNotFoundException.class})
    public ResponseEntity<Map<String, String>> bookingException(RuntimeException ex) {
        return ResponseEntity.status(ex instanceof BookingNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

}