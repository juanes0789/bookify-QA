package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class NonWorkingDayResponseDto {
    private UUID id;
    private UUID proveedorId;
    private LocalDate fecha;
    private String motivo;
    private OffsetDateTime fechaCreacion;
}
