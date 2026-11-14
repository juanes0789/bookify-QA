package udea.fabrica.bookify.domain.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.exception.BookingNotFoundException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.exception.InvalidReservationStateException;
import udea.fabrica.bookify.domain.exception.ReservationNotEndedException;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.model.BookingAttendanceInfo;
import udea.fabrica.bookify.domain.model.BookingStatusHistory;
import udea.fabrica.bookify.domain.model.RegisterAttendanceCommand;
import udea.fabrica.bookify.domain.port.in.RegisterAttendanceUseCase;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;

public class BookingAttendanceService implements RegisterAttendanceUseCase {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String ESTADO_CANCELADA   = "CANCELADA";
    private static final String ESTADO_CONFIRMADA  = "CONFIRMADA";
    private static final String ESTADO_ATENDIDA    = "ATENDIDA";
    private static final String ESTADO_NO_ASISTIO  = "NO_ASISTIO";

    private final BookingRepositoryPort bookingRepository;
    private final MerchantProfileRepositoryPort merchantProfileRepository;

    public BookingAttendanceService(BookingRepositoryPort bookingRepository,
                                    MerchantProfileRepositoryPort merchantProfileRepository) {
        this.bookingRepository = bookingRepository;
        this.merchantProfileRepository = merchantProfileRepository;
    }

    @Override
    @Transactional
    public Booking registerAttendance(RegisterAttendanceCommand command) {

        // 1. Validación de identidad del proveedor
        UUID providerId = merchantProfileRepository
                .findByUserId(command.authenticatedUserId())
                .orElseThrow(() -> new DomainForbiddenException(
                        "El usuario autenticado no tiene un perfil de proveedor asociado."))
                .getId();

        // 2. Existencia de la reserva
        BookingAttendanceInfo info = bookingRepository
                .findAttendanceInfoById(command.bookingId())
                .orElseThrow(() -> new BookingNotFoundException(
                        "No existe la reserva solicitada."));

        // 3. Validación de pertenencia (aislamiento de negocio)
        if (!info.providerId().equals(providerId)) {
            throw new DomainForbiddenException(
                    "No está autorizado para modificar reservas de otro negocio.");
        }

        // 4. Validación de estado actual
        String estadoActual = info.estadoCodigo().toUpperCase();
        if (ESTADO_CANCELADA.equals(estadoActual)) {
            throw new InvalidReservationStateException(
                    "No se puede registrar la asistencia de una reserva cancelada.");
        }
        if (ESTADO_ATENDIDA.equals(estadoActual) || ESTADO_NO_ASISTIO.equals(estadoActual)) {
            throw new DomainConflictException(
                    "La asistencia de esta reserva ya fue registrada previamente.");
        }
        if (!ESTADO_CONFIRMADA.equals(estadoActual)) {
            throw new DomainConflictException(
                    "Solo se puede registrar asistencia en reservas confirmadas.");
        }

        // 5. Validación temporal
        OffsetDateTime now = OffsetDateTime.now(BOGOTA);
        if (now.isBefore(info.finAt())) {
            throw new ReservationNotEndedException();
        }

        // 6. Transición y auditoría
        Integer newStatusId = bookingRepository.findReservationStatusIdByCode(command.status().name());
        bookingRepository.updateBookingStatus(command.bookingId(), newStatusId, now);

        String motivo = command.motivo() != null
                ? command.motivo()
                : "Registro de asistencia: " + command.status().name();

        BookingStatusHistory history = new BookingStatusHistory(
                command.bookingId(),
                info.estadoId(),
                newStatusId,
                command.authenticatedUserId(),
                motivo,
                now
        );
        bookingRepository.saveStatusHistory(history);

        // Retornar reserva actualizada
        return bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new BookingNotFoundException("No existe la reserva solicitada."));
    }
}

