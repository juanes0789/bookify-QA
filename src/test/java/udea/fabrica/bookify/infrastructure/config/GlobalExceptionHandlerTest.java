package udea.fabrica.bookify.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.BookingNotFoundException;
import udea.fabrica.bookify.domain.exception.CategoryNotFoundException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.exception.DoubleBookingException;
import udea.fabrica.bookify.domain.exception.EmailAlreadyExistsException;
import udea.fabrica.bookify.domain.exception.InvalidCancellationException;
import udea.fabrica.bookify.domain.exception.InvalidPasswordException;
import udea.fabrica.bookify.domain.exception.InvalidSearchCriteriaException;
import udea.fabrica.bookify.domain.exception.NonWorkingDayConflictException;
import udea.fabrica.bookify.domain.exception.PastDateException;
import udea.fabrica.bookify.domain.exception.LoginBlockedException;

import java.time.OffsetDateTime;

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

    @Test
    void shouldMapRegistrationErrorsToExpectedStatusCodes() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var emailTaken = handler.handleEmailAlreadyExists(new EmailAlreadyExistsException("El correo ya está registrado"));
        var invalidPassword = handler.handleInvalidPassword(new InvalidPasswordException("debe tener entre 15 y 64 caracteres"));

        // Assert
        assertThat(emailTaken.getStatusCode().value()).isEqualTo(409);
        assertThat(emailTaken.getBody()).containsEntry("mensaje", "El correo ya está registrado");
        assertThat(invalidPassword.getStatusCode().value()).isEqualTo(400);
        assertThat(invalidPassword.getBody()).containsEntry("motivo", "debe tener entre 15 y 64 caracteres");
    }

    @Test
    void shouldReportDuplicatedNonWorkingDay_whenUniqueDateConstraintFails() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var violation = new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("duplicate key value violates unique constraint \"uq_dia_no_laborable_proveedor_fecha\""));

        // Act
        var response = handler.handleDataIntegrityViolation(violation);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).containsEntry("mensaje", "La fecha ya está registrada como no laborable");
    }

    @Test
    void shouldMapNonWorkingDayErrorsToExpectedStatusCodes() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var pastDate = handler.handlePastDate(new PastDateException("La fecha debe ser igual o posterior a hoy"));
        var conflict = handler.handleNonWorkingDayConflict(new NonWorkingDayConflictException("La fecha está marcada como no laborable"));

        // Assert
        assertThat(pastDate.getStatusCode().value()).isEqualTo(400);
        assertThat(pastDate.getBody()).containsEntry("mensaje", "La fecha debe ser igual o posterior a hoy");
        assertThat(conflict.getStatusCode().value()).isEqualTo(409);
        assertThat(conflict.getBody()).containsEntry("mensaje", "La fecha está marcada como no laborable");
    }

    @Test
    void shouldMapInvalidSearchCriteriaToBadRequest() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var invalidCriteria = handler.handleSearchBadRequest(new InvalidSearchCriteriaException("Debes enviar un texto o una categoría"));
        var unknownCategory = handler.handleSearchBadRequest(new CategoryNotFoundException("Mascotas exóticas"));

        // Assert
        assertThat(invalidCriteria.getStatusCode().value()).isEqualTo(400);
        assertThat(invalidCriteria.getBody()).containsEntry("mensaje", "Debes enviar un texto o una categoría");
        assertThat(unknownCategory.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void shouldMapDomainForbiddenAndConflictToExpectedStatusCodes() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var forbidden = handler.handleForbidden(new DomainForbiddenException("Acceso no autorizado para el rol asignado"));
        var conflict = handler.handleConflict(new DomainConflictException("Ya tienes un perfil comercial registrado"));

        // Assert
        assertThat(forbidden.getStatusCode().value()).isEqualTo(403);
        assertThat(forbidden.getBody()).containsEntry("error", "Acceso no autorizado para el rol asignado");
        assertThat(conflict.getStatusCode().value()).isEqualTo(409);
        assertThat(conflict.getBody()).containsEntry("error", "Ya tienes un perfil comercial registrado");
    }

    @Test
    void shouldReturnRetryAfterHeader_whenLoginIsBlocked() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var response = handler.handleLoginBlocked(new LoginBlockedException(OffsetDateTime.now().plusSeconds(60)));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
        assertThat(Long.parseLong(response.getHeaders().getFirst("Retry-After"))).isBetween(59L, 60L);
    }

    @Test
    void shouldReturnZeroRetryAfter_whenBlockIsMissingOrExpired() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        var withoutDate = handler.handleLoginBlocked(new LoginBlockedException(null));
        var expired = handler.handleLoginBlocked(new LoginBlockedException(OffsetDateTime.now().minusSeconds(5)));

        // Assert
        assertThat(withoutDate.getHeaders().getFirst("Retry-After")).isEqualTo("0");
        assertThat(expired.getHeaders().getFirst("Retry-After")).isEqualTo("0");
    }

    @Test
    void shouldReturnBadRequest_whenNonWorkingDayReferencesUnknownProviderOrUser() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var violation = new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("insert violates foreign key constraint \"tbl_dia_no_laborable_proveedor_id_fkey\""));

        // Act
        var response = handler.handleDataIntegrityViolation(violation);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("mensaje", "El proveedor o usuario especificado no existe");
    }

    @Test
    void shouldReturnConflict_whenEmailUniqueConstraintFails() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var violation = new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("duplicate key value violates unique constraint \"uq_usuario_correo\""));

        // Act
        var response = handler.handleDataIntegrityViolation(violation);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).containsEntry("mensaje", "El correo ya está registrado");
    }
}
