package udea.fabrica.bookify.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.AttendanceMetrics;
import udea.fabrica.bookify.domain.model.AttendanceMetricsFilter;
import udea.fabrica.bookify.domain.model.AttendancePeriodSummary;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.GetAttendanceMetricsUseCase;
import udea.fabrica.bookify.domain.port.out.AttendanceMetricsOutputPort;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;

public class AttendanceMetricsService implements GetAttendanceMetricsUseCase {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");
    private static final String NO_DATA_MESSAGE = "Sin datos suficientes";

    private final AttendanceMetricsOutputPort metricsOutputPort;
    private final MerchantProfileRepositoryPort merchantProfileRepository;

    public AttendanceMetricsService(AttendanceMetricsOutputPort metricsOutputPort,
                                    MerchantProfileRepositoryPort merchantProfileRepository) {
        this.metricsOutputPort = metricsOutputPort;
        this.merchantProfileRepository = merchantProfileRepository;
    }

    @Override
    public AttendanceMetrics getMetrics(UUID authenticatedUserId, LocalDate fechaInicio, LocalDate fechaFin) {
        MerchantProfile profile = merchantProfileRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new DomainForbiddenException(
                        "El usuario no tiene un perfil comercial registrado."));
        AttendanceMetricsFilter filter = new AttendanceMetricsFilter(profile.getId(), fechaInicio, fechaFin);
        OffsetDateTime start = filter.fechaInicio().atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        OffsetDateTime end = filter.fechaFin().atTime(LocalTime.MAX).atZone(BUSINESS_ZONE).toOffsetDateTime();
        OffsetDateTime now = OffsetDateTime.now(BUSINESS_ZONE);
        AttendancePeriodSummary summary = metricsOutputPort.getPeriodSummary(profile.getId(), start, end, now);

        long total = summary.atendidas() + summary.noAsistidas();
        if (total == 0) {
            return new AttendanceMetrics(profile.getId(), fechaInicio, fechaFin, 0, 0, 0,
                    null, summary.confirmadasPasadas(), NO_DATA_MESSAGE);
        }
        BigDecimal rate = BigDecimal.valueOf(summary.noAsistidas()).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return new AttendanceMetrics(profile.getId(), fechaInicio, fechaFin, summary.atendidas(),
                summary.noAsistidas(), total, rate, summary.confirmadasPasadas(), null);
    }
}
