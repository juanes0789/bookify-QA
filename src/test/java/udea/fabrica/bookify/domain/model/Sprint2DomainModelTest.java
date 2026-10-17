package udea.fabrica.bookify.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class Sprint2DomainModelTest {

    @Test
    void shouldCountAffectedBookings_whenPreviewHasBookings() {
        // Arrange
        BlockingPreview preview = new BlockingPreview(UUID.randomUUID(), LocalDate.of(2026, 12, 25),
                List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

        // Act
        int affected = preview.affectedBookingsCount();

        // Assert
        assertThat(affected).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroAffectedBookings_whenPreviewIsEmpty() {
        // Arrange
        BlockingPreview preview = new BlockingPreview(UUID.randomUUID(), LocalDate.of(2026, 12, 25), List.of());

        // Act
        int affected = preview.affectedBookingsCount();

        // Assert
        assertThat(affected).isZero();
    }

    @Test
    void shouldKeepTextAndCategory_whenSearchFilterIsCreated() {
        // Arrange
        String text = "futbol";
        String category = "Deportes y recreación";

        // Act
        SearchServiceFilter filter = new SearchServiceFilter(text, category);

        // Assert
        assertThat(filter.getTexto()).isEqualTo("futbol");
        assertThat(filter.getCategoria()).isEqualTo("Deportes y recreación");
    }

    @Test
    void shouldBuildEqualProfiles_whenBuilderAndConstructorReceiveSameData() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        MerchantProfile built = MerchantProfile.builder().id(id).userId(userId).businessName("Barbería Central")
                .category("Barbería y belleza").address("Calle 10 # 43-20").phone("+573001234567").build();
        MerchantProfile constructed = new MerchantProfile(id, userId, "Barbería Central",
                "Barbería y belleza", "Calle 10 # 43-20", "+573001234567");

        // Assert
        assertThat(built).isEqualTo(constructed).hasSameHashCodeAs(constructed);
        assertThat(built.getBusinessName()).isEqualTo("Barbería Central");
    }

    @Test
    void shouldCreateRejectedAndAuthenticatedResults_whenLoginEnds() {
        // Arrange
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "ana@mail.com", UserRole.CLIENTE);

        // Act
        LoginResult authenticated = LoginResult.authenticated(user);
        LoginResult rejected = LoginResult.rejected(false, null);

        // Assert
        assertThat(authenticated.authenticated()).isTrue();
        assertThat(authenticated.user()).isEqualTo(user);
        assertThat(rejected.authenticated()).isFalse();
        assertThat(rejected.user()).isNull();
    }
}
