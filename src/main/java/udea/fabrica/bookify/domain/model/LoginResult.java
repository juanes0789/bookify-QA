package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;

public record LoginResult(boolean authenticated, boolean blocked, OffsetDateTime blockedUntil) { }
