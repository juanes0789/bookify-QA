package udea.fabrica.bookify.domain.model;

import java.util.UUID;

/** Identity data made available only after credentials have been verified. */
public record AuthenticatedUser(UUID id, String email, UserRole role) { }
