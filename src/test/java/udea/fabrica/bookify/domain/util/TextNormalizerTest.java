package udea.fabrica.bookify.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextNormalizerTest {

    @Test
    void shouldRemoveAccentsAndIgnoreCase() {
        assertThat(TextNormalizer.normalize("  Fútbol y recreación  "))
                .isEqualTo("futbol y recreacion");
    }

    @Test
    void shouldMatchPartialTextWithoutAccents() {
        assertThat(TextNormalizer.containsIgnoreCaseAndAccents("Barbería Central", "barberia cen"))
                .isTrue();
    }

    @Test
    void shouldNotMatchNullOrBlankTarget() {
        assertThat(TextNormalizer.containsIgnoreCaseAndAccents("Corte", null)).isFalse();
        assertThat(TextNormalizer.containsIgnoreCaseAndAccents("Corte", "  ")).isFalse();
    }
}
