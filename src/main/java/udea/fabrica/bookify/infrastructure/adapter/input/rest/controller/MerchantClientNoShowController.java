package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.port.in.GetClientNoShowReportUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ClientNoShowReportResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.ClientNoShowRestMapper;

@RestController
@RequestMapping("/api/v1/comercio/metricas/inasistencias")
public class MerchantClientNoShowController {
    private final GetClientNoShowReportUseCase getClientNoShowReportUseCase;

    public MerchantClientNoShowController(GetClientNoShowReportUseCase getClientNoShowReportUseCase) {
        this.getClientNoShowReportUseCase = getClientNoShowReportUseCase;
    }

    @GetMapping("/clientes")
    public ResponseEntity<ClientNoShowReportResponseDto> getClientsWithMostNoShows(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "limite", required = false, defaultValue = "50") Integer limite,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        var report = getClientNoShowReportUseCase.getReport(userId, fechaInicio, fechaFin, limite);
        return ResponseEntity.ok(ClientNoShowRestMapper.toDto(report));
    }
}
