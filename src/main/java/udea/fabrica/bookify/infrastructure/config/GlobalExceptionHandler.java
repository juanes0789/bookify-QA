package udea.fabrica.bookify.infrastructure.config;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.BookingNotFoundException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.exception.DoubleBookingException;
import udea.fabrica.bookify.domain.exception.InvalidCancellationException;
import udea.fabrica.bookify.domain.exception.*;

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
    public ResponseEntity<Map<String, String>> doubleBooking(DoubleBookingException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage())); }
    @ExceptionHandler({InvalidCancellationException.class, BookingNotFoundException.class})
    public ResponseEntity<Map<String, String>> bookingException(RuntimeException ex) {
        return ResponseEntity.status(ex instanceof BookingNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
 
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage(), "mensaje", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String causeMessage = ex.getMostSpecificCause() == null
                ? ex.getMessage()
                : ex.getMostSpecificCause().getMessage();
        if (causeMessage != null && causeMessage.contains("uq_dia_no_laborable_proveedor_fecha")) {
            String message = "La fecha ya está registrada como no laborable";
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", message, "mensaje", message));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El correo ya está registrado", "mensaje", "El correo ya está registrado"));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage(), "motivo", ex.getMessage(), "mensaje", ex.getMessage()));
    }

    @ExceptionHandler(PastDateException.class)
    public ResponseEntity<Map<String, String>> handlePastDate(PastDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage(), "mensaje", ex.getMessage()));
    }

    @ExceptionHandler(NonWorkingDayConflictException.class)
    public ResponseEntity<Map<String, String>> handleNonWorkingDayConflict(NonWorkingDayConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage(), "mensaje", ex.getMessage()));
    }

    @ExceptionHandler({InvalidSearchCriteriaException.class, CategoryNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleSearchBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage(), "mensaje", ex.getMessage()));
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
