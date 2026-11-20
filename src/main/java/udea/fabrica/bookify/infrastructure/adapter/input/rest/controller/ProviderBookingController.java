package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.model.ProviderBooking;
import udea.fabrica.bookify.domain.port.in.ProviderBookingHistoryUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ProviderBookingListResponseDto;

@RestController
@RequestMapping("/api/v1/comercio/reservas")
public class ProviderBookingController {

    private static final String BOM = "\uFEFF";

    private final ProviderBookingHistoryUseCase useCase;

    public ProviderBookingController(ProviderBookingHistoryUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<ProviderBookingListResponseDto> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication authentication) {

        List<ProviderBooking> bookings = useCase.getHistory(userId(authentication), role(authentication), desde, hasta);

        List<ProviderBookingListResponseDto.Item> items = bookings.stream()
                .map(b -> new ProviderBookingListResponseDto.Item(
                        b.fecha(), b.hora(), b.cliente(), b.servicio(), b.recurso(), b.estado()))
                .toList();
        String message = items.isEmpty() ? "No hay reservas en el rango de fechas seleccionado" : null;

        return ResponseEntity.ok(new ProviderBookingListResponseDto(items.size(), message, items));
    }

    @GetMapping("/exportar")
    public ResponseEntity<Object> export(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication authentication) {

        Optional<String> csv = useCase.exportCsv(userId(authentication), role(authentication), desde, hasta);

        if (csv.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No hay reservas en el rango de fechas seleccionado; no se generó el archivo"));
        }

        byte[] content = (BOM + csv.get()).getBytes(StandardCharsets.UTF_8);
        String filename = "historial_reservas_" + desde + "_" + hasta + ".csv";

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private String role(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}