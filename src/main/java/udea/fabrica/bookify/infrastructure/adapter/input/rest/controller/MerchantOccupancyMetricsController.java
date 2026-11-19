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
import udea.fabrica.bookify.domain.port.in.GetOccupancyMetricsUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.OccupancyMetricsRestMapper;

@RestController
@RequestMapping("/api/v1/comercio/metricas")
public class MerchantOccupancyMetricsController {
    private final GetOccupancyMetricsUseCase occupancy;
    public MerchantOccupancyMetricsController(GetOccupancyMetricsUseCase occupancy) { this.occupancy = occupancy; }
    @GetMapping("/ocupacion")
    public ResponseEntity<?> get(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                 Authentication authentication) {
        return ResponseEntity.ok(OccupancyMetricsRestMapper.toDto(
                occupancy.getMetrics(UUID.fromString(authentication.getName()), fechaInicio, fechaFin)));
    }
}
