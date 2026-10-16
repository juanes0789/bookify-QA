package udea.fabrica.bookify.domain.model;

import java.util.UUID;

public record Resource(UUID id, UUID providerId, UUID resourceTypeId, String name) { }
