package udea.fabrica.bookify.domain.model;
import java.time.OffsetDateTime; import java.util.UUID;
public record BookingStatusHistory(UUID reservaId, Integer estadoAnteriorId, Integer estadoNuevoId, UUID cambiadoPorUsuarioId, String motivo, OffsetDateTime fechaCambio) {}
