package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.LoginRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.NonWorkingDayRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.RegisterNonWorkingDayRequestDto;

/** Validaciones de entrada de HU-07, HU-09 y HU-10 declaradas en los DTO. */
class Sprint2RequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void shouldAcceptMerchantProfile_whenAllFieldsAreValid() {
        // Arrange
        CreateMerchantProfileRequest request = merchantRequest("Barbería Central", "Barbería y belleza",
                "Calle 10 # 43-20, Medellín", "+573001234567");

        // Act
        Set<ConstraintViolation<CreateMerchantProfileRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldListMissingFields_whenBusinessNameAndCategoryAreMissing() {
        // Arrange
        CreateMerchantProfileRequest request = merchantRequest(null, " ", "Calle 10 # 43-20, Medellín", "+573001234567");

        // Act
        Set<String> invalidFields = invalidFields(validator.validate(request));

        // Assert
        assertThat(invalidFields).containsExactlyInAnyOrder("businessName", "category");
    }

    @Test
    void shouldRejectPhone_whenItIsNotE164() {
        // Arrange
        CreateMerchantProfileRequest request = merchantRequest("Barbería Central", "Barbería y belleza",
                "Calle 10 # 43-20, Medellín", "3001234567");

        // Act
        Set<ConstraintViolation<CreateMerchantProfileRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactly("El teléfono debe tener formato internacional E.164");
    }

    @Test
    void shouldRejectBusinessName_whenItHasLessThanThreeCharacters() {
        // Arrange
        CreateMerchantProfileRequest request = merchantRequest("BC", "Barbería y belleza",
                "Calle 10 # 43-20, Medellín", "+573001234567");

        // Act
        Set<String> invalidFields = invalidFields(validator.validate(request));

        // Assert
        assertThat(invalidFields).containsExactly("businessName");
    }

    @Test
    void shouldRejectReason_whenItExceedsOneHundredCharacters() {
        // Arrange
        RegisterNonWorkingDayRequestDto request = new RegisterNonWorkingDayRequestDto();
        request.setFecha(LocalDate.of(2026, 12, 25));
        request.setMotivo("x".repeat(101));

        // Act
        Set<String> invalidFields = invalidFields(validator.validate(request));

        // Assert
        assertThat(invalidFields).containsExactly("motivo");
    }

    @Test
    void shouldRequireDate_whenNonWorkingDayIsRegistered() {
        // Arrange
        RegisterNonWorkingDayRequestDto request = new RegisterNonWorkingDayRequestDto();
        request.setMotivo("Navidad");

        // Act
        Set<ConstraintViolation<RegisterNonWorkingDayRequestDto>> violations = validator.validate(request);

        // Assert
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactly("La fecha es obligatoria");
    }

    @Test
    void shouldRequireProviderDateAndCreator_whenBlockingRequestIsEmpty() {
        // Arrange
        NonWorkingDayRequestDto request = new NonWorkingDayRequestDto(null, null, "Navidad", null);

        // Act
        Set<String> invalidFields = invalidFields(validator.validate(request));

        // Assert
        assertThat(invalidFields).containsExactlyInAnyOrder("idProveedor", "fecha", "idUsuarioCreador");
    }

    @Test
    void shouldAcceptBlockingRequest_whenReasonIsOmitted() {
        // Arrange
        NonWorkingDayRequestDto request = new NonWorkingDayRequestDto(UUID.randomUUID(),
                LocalDate.of(2026, 12, 25), null, UUID.randomUUID());

        // Act
        Set<ConstraintViolation<NonWorkingDayRequestDto>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectLogin_whenEmailIsInvalidAndPasswordIsBlank() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto("correo-sin-arroba", " ");

        // Act
        Set<String> invalidFields = invalidFields(validator.validate(request));

        // Assert
        assertThat(invalidFields).containsExactlyInAnyOrder("correo", "password");
    }

    private static CreateMerchantProfileRequest merchantRequest(String name, String category, String address, String phone) {
        CreateMerchantProfileRequest request = new CreateMerchantProfileRequest();
        request.setBusinessName(name);
        request.setCategory(category);
        request.setAddress(address);
        request.setPhone(phone);
        return request;
    }

    private static <T> Set<String> invalidFields(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
