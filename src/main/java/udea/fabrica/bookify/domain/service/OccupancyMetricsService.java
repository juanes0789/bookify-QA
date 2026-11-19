package udea.fabrica.bookify.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.model.OccupancyDaily;
import udea.fabrica.bookify.domain.model.OccupancyMetrics;
import udea.fabrica.bookify.domain.model.OccupancySlot;
import udea.fabrica.bookify.domain.model.OccupancySourceData;
import udea.fabrica.bookify.domain.port.in.GetOccupancyMetricsUseCase;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.OccupancyMetricsOutputPort;

public class OccupancyMetricsService implements GetOccupancyMetricsUseCase {
    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final String NO_DATA = "Sin datos para el periodo seleccionado";
    private final OccupancyMetricsOutputPort output;
    private final MerchantProfileRepositoryPort profiles;

    public OccupancyMetricsService(OccupancyMetricsOutputPort output, MerchantProfileRepositoryPort profiles) {
        this.output = output;
        this.profiles = profiles;
    }

    @Override
    public OccupancyMetrics getMetrics(UUID userId, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) throw new IllegalArgumentException("El periodo seleccionado no es válido.");
        MerchantProfile profile = profiles.findByUserId(userId).orElseThrow(() ->
                new DomainForbiddenException("El usuario no tiene un perfil comercial registrado."));
        OffsetDateTime start = from.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime end = to.atTime(LocalTime.MAX).atZone(ZONE).toOffsetDateTime();
        OccupancySourceData data = output.getSourceData(profile.getId(), start, end);
        List<OccupancyDaily> daily = new ArrayList<>();
        long available = 0, blocked = 0, booked = 0;
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            OffsetDateTime dayStart = day.atStartOfDay(ZONE).toOffsetDateTime();
            OffsetDateTime dayEnd = day.plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();
            long a = minutes(data.availableSlots(), dayStart, dayEnd);
            long b = minutes(data.blockedSlots(), dayStart, dayEnd);
            long o = Math.min(a, minutes(data.bookedSlots(), dayStart, dayEnd));
            BigDecimal rate = a == 0 ? null : percent(o, a);
            daily.add(new OccupancyDaily(day, a, b, o, rate));
            available += a; blocked += b; booked += o;
        }
        if (available == 0) return new OccupancyMetrics(profile.getId(), from, to, 0, blocked, 0, null, daily, NO_DATA);
        return new OccupancyMetrics(profile.getId(), from, to, available, blocked, booked,
                percent(booked, available), daily, null);
    }

    private long minutes(List<OccupancySlot> slots, OffsetDateTime start, OffsetDateTime end) {
        return slots.stream().mapToLong(slot -> overlapMinutes(slot, start, end)).sum();
    }

    private long overlapMinutes(OccupancySlot slot, OffsetDateTime start, OffsetDateTime end) {
        OffsetDateTime from = slot.startAt().isAfter(start) ? slot.startAt() : start;
        OffsetDateTime to = slot.endAt().isBefore(end) ? slot.endAt() : end;
        return to.isAfter(from) ? Duration.between(from, to).toMinutes() : 0;
    }

    private BigDecimal percent(long numerator, long denominator) {
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
