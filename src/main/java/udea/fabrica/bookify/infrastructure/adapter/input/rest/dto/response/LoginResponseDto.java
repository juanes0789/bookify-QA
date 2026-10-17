package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.time.OffsetDateTime;

/** Response returned for a rejected login attempt. */
public record LoginResponseDto(boolean authenticated, boolean blocked, OffsetDateTime blockedUntil) { }
