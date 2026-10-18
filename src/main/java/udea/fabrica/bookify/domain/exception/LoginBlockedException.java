package udea.fabrica.bookify.domain.exception;

import java.time.OffsetDateTime;

public class LoginBlockedException extends RuntimeException {
    private final OffsetDateTime blockedUntil;

    public LoginBlockedException(OffsetDateTime blockedUntil) {
        super("La cuenta está bloqueada temporalmente.");
        this.blockedUntil = blockedUntil;
    }

    public OffsetDateTime getBlockedUntil() {
        return blockedUntil;
    }
}
