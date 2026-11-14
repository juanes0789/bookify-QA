package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ClientNoShowReport(
        UUID providerId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<ClientNoShowSummary> clientes,
        String mensaje) {

    public ClientNoShowReport {
        clientes = clientes == null ? List.of() : List.copyOf(clientes);
    }

    public int totalClientes() {
        return clientes.size();
    }
}
