package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
public record NonWorkingDayRequestDto(@NotNull UUID idProveedor, @NotNull LocalDate fecha, String motivo, @NotNull UUID idUsuarioCreador) { }
