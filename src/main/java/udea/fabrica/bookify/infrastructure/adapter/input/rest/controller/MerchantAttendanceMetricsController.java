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
import udea.fabrica.bookify.domain.port.in.GetAttendanceMetricsUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.AttendanceMetricsResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.AttendanceMetricsRestMapper;

@RestController
@RequestMapping("/api/v1/comercio/metricas")
public class MerchantAttendanceMetricsController {
    private final GetAttendanceMetricsUseCase getAttendanceMetricsUseCase;

    public MerchantAttendanceMetricsController(GetAttendanceMetricsUseCase getAttendanceMetricsUseCase) {
        this.getAttendanceMetricsUseCase = getAttendanceMetricsUseCase;
    }

    @GetMapping("/inasistencias")
    public ResponseEntity<AttendanceMetricsResponseDto> getAttendanceMetrics(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        var metrics = getAttendanceMetricsUseCase.getMetrics(userId, fechaInicio, fechaFin);
        return ResponseEntity.ok(AttendanceMetricsRestMapper.toDto(metrics));
    }
}
