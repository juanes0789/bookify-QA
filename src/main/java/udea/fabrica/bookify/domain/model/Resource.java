package udea.fabrica.bookify.domain.model;

import java.util.UUID;

import lombok.Builder;

@Builder 
public record Resource(UUID id, UUID providerId, UUID resourceTypeId, String name) { }
