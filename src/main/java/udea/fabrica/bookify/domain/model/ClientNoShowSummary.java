package udea.fabrica.bookify.domain.model;

import java.util.UUID;

public record ClientNoShowSummary(
        UUID clienteId,
        String nombre,
        String correo,
        String telefono,
        long inasistencias) {
}
