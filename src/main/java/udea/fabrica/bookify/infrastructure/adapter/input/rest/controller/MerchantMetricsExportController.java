package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.port.in.ExportMetricsUseCase;

@RestController
@RequestMapping("/api/v1/comercio/metricas")
public class MerchantMetricsExportController {
    private final ExportMetricsUseCase exporter;
    public MerchantMetricsExportController(ExportMetricsUseCase exporter) { this.exporter = exporter; }
    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> export(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                         Authentication authentication) {
        byte[] body = exporter.export(UUID.fromString(authentication.getName()), fechaInicio, fechaFin)
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("metricas-citas.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(body);
    }
}
