package udea.fabrica.bookify.infrastructure.adapter.output.security;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.port.out.CommonPasswordPort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class ResourceCommonPasswordAdapter implements CommonPasswordPort {

    private final Set<String> commonPasswords = new HashSet<>();

    @PostConstruct
    void loadPasswords() {
        try {
            ClassPathResource resource = new ClassPathResource("passwords/common-passwords.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        commonPasswords.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar la lista de contraseñas comunes", e);
        }
    }

    @Override
    public boolean isCommon(String password) {
        if (password == null) return false;
        return commonPasswords.contains(password.toLowerCase());
    }
}
