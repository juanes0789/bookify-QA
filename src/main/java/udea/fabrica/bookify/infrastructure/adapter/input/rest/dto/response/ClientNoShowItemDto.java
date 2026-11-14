package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.util.UUID;

public record ClientNoShowItemDto(
        UUID clienteId,
        String nombre,
        String correo,
        String telefono,
        long inasistencias) {
}
