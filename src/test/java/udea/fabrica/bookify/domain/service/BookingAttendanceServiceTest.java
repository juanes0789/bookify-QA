package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import udea.fabrica.bookify.domain.exception.BookingNotFoundException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.AttendanceStatus;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.model.BookingAttendanceInfo;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.model.RegisterAttendanceCommand;
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;

@ExtendWith(MockitoExtension.class)
class BookingAttendanceServiceTest {

    @Mock private BookingRepositoryPort bookingRepository;
    @Mock private MerchantProfileRepositoryPort merchantProfileRepository;

    @InjectMocks private BookingAttendanceService service;

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private UUID userId;
    private UUID providerId;
    private UUID bookingId;
    private MerchantProfile profile;

    @BeforeEach
    void setUp() {
        userId     = UUID.randomUUID();
        providerId = UUID.randomUUID();
        bookingId  = UUID.randomUUID();
        profile = MerchantProfile.builder()
                .id(providerId)
                .userId(userId)
                .businessName("Mi Negocio")
                .build();
    }

    // ─── Escenario 1: Registrar cita atendida ───────────────────────────────────

    @Test
    void shouldRegisterAttendedSuccessfully() {
        // Arrange
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusMinutes(10);
        Integer oldStatusId = 2;
        Integer newStatusId = 10;

        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "CONFIRMADA", oldStatusId, finAt);
        Booking updatedBooking = buildBooking(newStatusId);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));
        when(bookingRepository.findReservationStatusIdByCode("ATENDIDA")).thenReturn(newStatusId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(updatedBooking));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, "Cita completada");

        // Act
        Booking result = service.registerAttendance(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        verify(bookingRepository).updateBookingStatus(eq(bookingId), eq(newStatusId), any());
        verify(bookingRepository).saveStatusHistory(any());
    }

    // ─── Escenario 2: Registrar inasistencia ────────────────────────────────────

    @Test
    void shouldRegisterNoShowSuccessfully() {
        // Arrange
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusMinutes(5);
        Integer oldStatusId = 2;
        Integer newStatusId = 11;

        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "CONFIRMADA", oldStatusId, finAt);
        Booking updatedBooking = buildBooking(newStatusId);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));
        when(bookingRepository.findReservationStatusIdByCode("NO_ASISTIO")).thenReturn(newStatusId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(updatedBooking));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.NO_ASISTIO, null);

        // Act
        Booking result = service.registerAttendance(command);

        // Assert
        assertThat(result).isNotNull();
        verify(bookingRepository).updateBookingStatus(eq(bookingId), eq(newStatusId), any());
        verify(bookingRepository).saveStatusHistory(any());
    }

    // ─── Escenario 3: Impedir registrar asistencia anticipadamente ───────────────

    @Test
    void shouldRejectWhenReservationNotEnded() {
        // Arrange
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).plusMinutes(30); // aún no ha finalizado
        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "CONFIRMADA", 2, finAt);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("todavía no ha finalizado");

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
        verify(bookingRepository, never()).saveStatusHistory(any());
    }

    // ─── Escenario 4: Impedir clasificar reservas canceladas ────────────────────

    @Test
    void shouldRejectWhenReservationIsCancelled() {
        // Arrange
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusMinutes(10);
        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "CANCELADA", 3, finAt);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("cancelada");

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
        verify(bookingRepository, never()).saveStatusHistory(any());
    }

    // ─── Escenario 5: Impedir modificar reservas de otro negocio ────────────────

    @Test
    void shouldRejectWhenBookingBelongsToAnotherProvider() {
        // Arrange
        UUID otroProviderId = UUID.randomUUID(); // diferente al del perfil autenticado
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusMinutes(10);
        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, otroProviderId, "CONFIRMADA", 2, finAt);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainForbiddenException.class)
                .hasMessageContaining("otro negocio");

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
        verify(bookingRepository, never()).saveStatusHistory(any());
    }

    // ─── Escenario 6: Reserva inexistente ───────────────────────────────────────

    @Test
    void shouldFailWhenBookingNotFound() {
        // Arrange
        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.empty());

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("No existe la reserva solicitada");

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
    }

    // ─── Escenario 7: Usuario sin perfil de proveedor ───────────────────────────

    @Test
    void shouldRejectWhenUserHasNoMerchantProfile() {
        // Arrange
        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainForbiddenException.class);

        verify(bookingRepository, never()).findAttendanceInfoById(any());
        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
    }

    // ─── Escenario 8: Impedir reclasificar reserva ya marcada ───────────────────

    @Test
    void shouldRejectWhenAttendanceAlreadyRegistered() {
        // Arrange
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusMinutes(60);
        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "ATENDIDA", 10, finAt);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.NO_ASISTIO, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("ya fue registrada previamente");

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
    }

    // ─── Escenario 9: Estado inválido distinto a CONFIRMADA/CANCELADA/ATENDIDA/NO_ASISTIO ──

    @Test
    void shouldRejectWhenReservationIsNotConfirmed() {
        // Arrange
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusMinutes(5);
        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "PENDIENTE", 1, finAt);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("confirmadas");

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
    }

    // ─── Límite temporal exacto: 1 segundo antes del fin ───────────────────────

    @Test
    void shouldRejectWhenOneSecondBeforeEnd() {
        // Arrange - finAt es exactamente en 1 segundo (aún no finaliza)
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).plusSeconds(1);
        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "CONFIRMADA", 2, finAt);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act / Assert
        assertThatThrownBy(() -> service.registerAttendance(command))
                .isInstanceOf(DomainConflictException.class);

        verify(bookingRepository, never()).updateBookingStatus(any(), any(), any());
    }

    // ─── Límite temporal exacto: 1 segundo después del fin ─────────────────────

    @Test
    void shouldAllowWhenOneSecondAfterEnd() {
        // Arrange - finAt fue hace 1 segundo (ya finalizó)
        OffsetDateTime finAt = OffsetDateTime.now(BOGOTA).minusSeconds(1);
        Integer oldStatusId = 2;
        Integer newStatusId = 10;

        BookingAttendanceInfo info = new BookingAttendanceInfo(
                bookingId, providerId, "CONFIRMADA", oldStatusId, finAt);
        Booking updatedBooking = buildBooking(newStatusId);

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(bookingRepository.findAttendanceInfoById(bookingId)).thenReturn(Optional.of(info));
        when(bookingRepository.findReservationStatusIdByCode("ATENDIDA")).thenReturn(newStatusId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(updatedBooking));

        RegisterAttendanceCommand command = new RegisterAttendanceCommand(
                bookingId, userId, AttendanceStatus.ATENDIDA, null);

        // Act
        Booking result = service.registerAttendance(command);

        // Assert
        assertThat(result).isNotNull();
        verify(bookingRepository).updateBookingStatus(eq(bookingId), eq(newStatusId), any());
    }

    // ─── Helper ─────────────────────────────────────────────────────────────────

    private Booking buildBooking(Integer statusId) {
        Booking b = new Booking();
        b.setId(bookingId);
        b.setEstadoReservaId(statusId);
        b.setFechaActualizacion(OffsetDateTime.now(BOGOTA));
        return b;
    }
}

