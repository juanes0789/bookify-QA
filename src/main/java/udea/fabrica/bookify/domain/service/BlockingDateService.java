package udea.fabrica.bookify.domain.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.model.BlockingPreview;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.in.ConfirmarBloqueoFechaUseCase;
import udea.fabrica.bookify.domain.port.in.PrevisualizarBloqueoFechaUseCase;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;

public class BlockingDateService implements PrevisualizarBloqueoFechaUseCase, ConfirmarBloqueoFechaUseCase {
    private final NonWorkingDayPort days;
    public BlockingDateService(NonWorkingDayPort days) { this.days = days; }
    @Override public BlockingPreview previsualizar(UUID providerId, LocalDate date) {
        require(providerId, date); return new BlockingPreview(providerId, date, days.findAffectedBookingIds(providerId, date));
    }
    @Override @Transactional public NonWorkingDay confirmar(NonWorkingDay day) {
        require(day.getProveedorId(), day.getFecha());
        NonWorkingDay toSave = new NonWorkingDay(day.getId() == null ? UUID.randomUUID() : day.getId(), day.getProveedorId(), day.getFecha(),
                day.getMotivo(), day.getCreatedByUserId(), day.getFechaCreacion() == null ? OffsetDateTime.now() : day.getFechaCreacion());
        return days.save(toSave);
    }
    private void require(UUID providerId, LocalDate date) { if (providerId == null || date == null) throw new IllegalArgumentException("Proveedor y fecha son requeridos."); }
}
