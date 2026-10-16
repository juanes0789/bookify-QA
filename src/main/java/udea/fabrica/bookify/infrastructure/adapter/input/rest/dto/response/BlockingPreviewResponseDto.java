package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record BlockingPreviewResponseDto(UUID idProveedor, LocalDate fecha, int reservasAfectadas, List<UUID> idsReservasAfectadas) { }
