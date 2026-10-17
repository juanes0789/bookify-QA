package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;

public record LoginResult(boolean authenticated, boolean blocked, OffsetDateTime blockedUntil,
                          AuthenticatedUser user) {
    public static LoginResult authenticated(AuthenticatedUser user) {
        return new LoginResult(true, false, null, user);
    }

    public static LoginResult rejected(boolean blocked, OffsetDateTime blockedUntil) {
        return new LoginResult(false, blocked, blockedUntil, null);
    }
}
