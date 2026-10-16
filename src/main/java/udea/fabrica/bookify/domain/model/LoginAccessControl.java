package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;

/** Aggregate that deliberately exists independently from Usuario. */
public record LoginAccessControl(String normalizedEmail, int failedAttempts,
                                 OffsetDateTime blockedUntil, OffsetDateTime lastFailedAttemptAt) {
    public boolean isBlockedAt(OffsetDateTime instant) {
        return blockedUntil != null && blockedUntil.isAfter(instant);
    }
}
