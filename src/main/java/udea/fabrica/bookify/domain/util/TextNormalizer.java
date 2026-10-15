package udea.fabrica.bookify.domain.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public static boolean containsIgnoreCaseAndAccents(String source, String target) {
        String normalizedSource = normalize(source);
        String normalizedTarget = normalize(target);
        if (normalizedSource == null || normalizedTarget == null || normalizedTarget.isBlank()) {
            return false;
        }
        return normalizedSource.contains(normalizedTarget);
    }
}
