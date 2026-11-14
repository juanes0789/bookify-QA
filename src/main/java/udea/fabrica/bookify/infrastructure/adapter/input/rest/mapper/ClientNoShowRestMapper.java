package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import udea.fabrica.bookify.domain.model.ClientNoShowReport;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ClientNoShowItemDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ClientNoShowReportResponseDto;

public final class ClientNoShowRestMapper {
    private ClientNoShowRestMapper() {
    }

    public static ClientNoShowReportResponseDto toDto(ClientNoShowReport report) {
        return new ClientNoShowReportResponseDto(report.providerId(), report.fechaInicio(), report.fechaFin(),
                report.totalClientes(), report.clientes().stream()
                        .map(client -> new ClientNoShowItemDto(client.clienteId(), client.nombre(),
                                client.correo(), client.telefono(), client.inasistencias()))
                        .toList(), report.mensaje());
    }
}
