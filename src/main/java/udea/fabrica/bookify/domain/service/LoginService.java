package udea.fabrica.bookify.domain.service;

import java.time.OffsetDateTime;
import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.exception.LoginBlockedException;
import udea.fabrica.bookify.domain.model.LoginAccessControl;
import udea.fabrica.bookify.domain.model.LoginResult;
import udea.fabrica.bookify.domain.port.in.LoginUseCase;
import udea.fabrica.bookify.domain.port.out.LoginAccessControlPort;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;

public class LoginService implements LoginUseCase {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final LoginAccessControlPort accessControls;
    private final LoginAuthenticationPort authentication;
    public LoginService(LoginAccessControlPort accessControls, LoginAuthenticationPort authentication) {
        this.accessControls = accessControls; this.authentication = authentication;
    }
    @Override @Transactional
    public LoginResult login(String email, String password) {
        String normalized = normalize(email); OffsetDateTime now = OffsetDateTime.now();
        LoginAccessControl previous = accessControls.findByNormalizedEmail(normalized)
                .orElse(new LoginAccessControl(normalized, 0, null, null));
        if (previous.isBlockedAt(now)) throw new LoginBlockedException(previous.blockedUntil());
        if (authentication.credentialsMatch(normalized, password)) {
            accessControls.clear(normalized); return new LoginResult(true, false, null);
        }
        int attempts = previous.failedAttempts() + 1;
        OffsetDateTime until = attempts >= MAX_FAILED_ATTEMPTS ? now.plusMinutes(15) : null;
        accessControls.save(new LoginAccessControl(normalized, attempts, until, now));
        return new LoginResult(false, until != null, until);
    }
    private String normalize(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("El correo es requerido.");
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
