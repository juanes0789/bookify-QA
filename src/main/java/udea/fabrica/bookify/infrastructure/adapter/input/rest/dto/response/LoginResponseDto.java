package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.time.OffsetDateTime;
public record LoginResponseDto(boolean autenticado, boolean bloqueado, OffsetDateTime bloqueadoHasta) { }
