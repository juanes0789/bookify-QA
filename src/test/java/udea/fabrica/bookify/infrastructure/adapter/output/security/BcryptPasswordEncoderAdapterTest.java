package udea.fabrica.bookify.infrastructure.adapter.output.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BcryptPasswordEncoderAdapterTest {

    @Test
    void shouldStoreVerifiableBcryptHashWithCost12_whenPasswordIsEncoded() {
        // Arrange
        BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();
        String rawPassword = "MiContrasenaSegura2026!";

        // Act
        String hash = adapter.encode(rawPassword);

        // Assert
        assertThat(hash).isNotEqualTo(rawPassword).startsWith("$2a$12$");
        assertThat(new BCryptPasswordEncoder().matches(rawPassword, hash)).isTrue();
    }

    @Test
    void shouldGenerateDifferentHashes_whenSamePasswordIsEncodedTwice() {
        // Arrange
        BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();

        // Act
        String first = adapter.encode("MiContrasenaSegura2026!");
        String second = adapter.encode("MiContrasenaSegura2026!");

        // Assert
        assertThat(first).isNotEqualTo(second);
    }
}
