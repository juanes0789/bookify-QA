package udea.fabrica.bookify.domain.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.ClientNoShowReport;
import udea.fabrica.bookify.domain.model.ClientNoShowSummary;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.GetClientNoShowReportUseCase;
import udea.fabrica.bookify.domain.port.out.ClientNoShowOutputPort;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;

public class ClientNoShowService implements GetClientNoShowReportUseCase {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");
    private static final int DEFAULT_LIMIT = 50;
    private static final String EMPTY_REPORT_MESSAGE =
            "No existen inasistencias registradas en el periodo seleccionado.";

    private final ClientNoShowOutputPort clientNoShowOutputPort;
    private final MerchantProfileRepositoryPort merchantProfileRepository;

    public ClientNoShowService(ClientNoShowOutputPort clientNoShowOutputPort,
                               MerchantProfileRepositoryPort merchantProfileRepository) {
        this.clientNoShowOutputPort = clientNoShowOutputPort;
        this.merchantProfileRepository = merchantProfileRepository;
    }

    @Override
    public ClientNoShowReport getReport(UUID authenticatedUserId, LocalDate fechaInicio,
                                        LocalDate fechaFin, Integer limite) {
        MerchantProfile profile = merchantProfileRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new DomainForbiddenException(
                        "El usuario no tiene un perfil comercial registrado."));
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        int effectiveLimit = limite == null ? DEFAULT_LIMIT : limite;
        if (effectiveLimit <= 0) {
            throw new IllegalArgumentException("El límite debe ser un entero positivo.");
        }

        OffsetDateTime start = fechaInicio.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        OffsetDateTime end = fechaFin.atTime(LocalTime.MAX).atZone(BUSINESS_ZONE).toOffsetDateTime();
        List<ClientNoShowSummary> clients = clientNoShowOutputPort.findClientsWithMostNoShows(
                profile.getId(), start, end, effectiveLimit);
        List<ClientNoShowSummary> safeClients = clients == null ? List.of() : List.copyOf(clients);
        String message = safeClients.isEmpty() ? EMPTY_REPORT_MESSAGE : null;
        return new ClientNoShowReport(profile.getId(), fechaInicio, fechaFin, safeClients, message);
    }
}
