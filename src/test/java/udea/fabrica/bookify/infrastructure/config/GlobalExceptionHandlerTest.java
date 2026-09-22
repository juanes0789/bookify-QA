package udea.fabrica.bookify.infrastructure.config;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.BookingNotFoundException;
import udea.fabrica.bookify.domain.exception.DoubleBookingException;
import udea.fabrica.bookify.domain.exception.InvalidCancellationException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void shouldMapOverlapToConflict() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var response = handler.handleOverlapException(new AvailabilityOverlapException("Overlap"));

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).containsEntry("error", "Overlap");
    }

    @Test
    void shouldMapIllegalArgumentToBadRequest() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var response = handler.handleIllegalArgument(new IllegalArgumentException("Bad request"));

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "Bad request");
    }

    @Test
    void shouldMapBookingExceptionsToExpectedStatusCodes() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var doubleBooking = handler.doubleBooking(new DoubleBookingException("Duplicate"));
        var invalidCancellation = handler.bookingException(new InvalidCancellationException("Invalid"));
        var notFound = handler.bookingException(new BookingNotFoundException("Not found"));

        // Assert
        assertThat(doubleBooking.getStatusCode().value()).isEqualTo(409);
        assertThat(invalidCancellation.getStatusCode().value()).isEqualTo(400);
        assertThat(notFound.getStatusCode().value()).isEqualTo(404);
    }
}
