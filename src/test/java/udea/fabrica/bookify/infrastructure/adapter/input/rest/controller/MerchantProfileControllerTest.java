package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.MerchantProfileUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

class MerchantProfileControllerTest {

    private MerchantProfileUseCase useCase;
    private MerchantProfileController controller;

    @BeforeEach
    void setUp() {
        useCase = mock(MerchantProfileUseCase.class);
        controller = new MerchantProfileController(useCase);
    }

    @Test
    void shouldReturn201WithProfile_whenProviderCreatesProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), "token", List.of(new SimpleGrantedAuthority("PROVEEDOR")));
        CreateMerchantProfileRequest request = new CreateMerchantProfileRequest();
        request.setBusinessName("Barbería Central");
        MerchantProfile created = MerchantProfile.builder()
                .id(UUID.randomUUID()).userId(userId).businessName("Barbería Central").build();
        when(useCase.createProfile(userId, "PROVEEDOR", request)).thenReturn(created);

        // Act
        ResponseEntity<MerchantProfile> response = controller.createProfile(request, authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
    }
}
