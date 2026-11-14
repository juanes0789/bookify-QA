package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ClientNoShowReportResponseDto(
        UUID proveedorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int totalClientes,
        List<ClientNoShowItemDto> clientes,
        String mensaje) {
    public ClientNoShowReportResponseDto {
        clientes = clientes == null ? List.of() : List.copyOf(clientes);
    }
}
