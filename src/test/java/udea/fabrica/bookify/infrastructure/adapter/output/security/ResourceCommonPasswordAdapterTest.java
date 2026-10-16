package udea.fabrica.bookify.infrastructure.adapter.output.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCommonPasswordAdapterTest {

    private ResourceCommonPasswordAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ResourceCommonPasswordAdapter();
        adapter.loadPasswords();
    }

    @Test
    void shouldDetectKnownCommonPassword() {
        assertThat(adapter.isCommon("contraseña123456")).isTrue();
    }

    @Test
    void shouldDetectCommonPasswordCaseInsensitive() {
        assertThat(adapter.isCommon("CONTRASEÑA123456")).isTrue();
    }

    @Test
    void shouldNotFlagStrongPassword() {
        assertThat(adapter.isCommon("Tr0ub4dor&3-XyZw!")).isFalse();
    }

    @Test
    void shouldReturnFalseForNullPassword() {
        assertThat(adapter.isCommon(null)).isFalse();
    }

    @Test
    void shouldDetectPassword123456789() {
        assertThat(adapter.isCommon("password123456789")).isTrue();
    }
}
