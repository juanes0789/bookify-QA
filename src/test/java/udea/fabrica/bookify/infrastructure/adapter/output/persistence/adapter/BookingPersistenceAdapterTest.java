package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.model.BookingStatusHistory;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.HistorialEstadoReservaEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.OcupacionDisponibilidadEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ReservaEntity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingPersistenceAdapterTest {

    @Test
    void shouldPersistNewBookingWhenIdIsNull() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        Booking booking = bookingWithoutId();
        UUID generatedId = UUID.randomUUID();
        doAnswer(invocation -> {
            ReservaEntity entity = invocation.getArgument(0);
            entity.setId(generatedId);
            return null;
        }).when(entityManager).persist(any(ReservaEntity.class));

        // Act
        Booking result = adapter.save(booking);

        // Assert
        verify(entityManager).persist(any(ReservaEntity.class));
        verify(entityManager, never()).merge(any(ReservaEntity.class));
        assertThat(result.getId()).isEqualTo(generatedId);
    }

    @Test
    void shouldMergeExistingBookingWhenIdAlreadyExists() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        Booking booking = bookingWithoutId();
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);
        ReservaEntity existing = new ReservaEntity();
        existing.setId(bookingId);
        when(entityManager.find(ReservaEntity.class, bookingId)).thenReturn(existing);

        // Act
        Booking result = adapter.save(booking);

        // Assert
        verify(entityManager).merge(existing);
        verify(entityManager, never()).persist(existing);
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(existing.getDisponibilidadId()).isEqualTo(booking.getAvailabilityId());
    }

    @Test
    void shouldFindBookingByIdAndMapEntityFields() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        UUID bookingId = UUID.randomUUID();
        ReservaEntity entity = new ReservaEntity();
        entity.setId(bookingId);
        entity.setClienteId(UUID.randomUUID());
        entity.setEstadoReservaId(10);
        entity.setDisponibilidadId(UUID.randomUUID());
        entity.setFechaReserva(OffsetDateTime.parse("2026-10-11T09:00:00Z"));
        entity.setNotas("Notas");
        entity.setFechaCreacion(OffsetDateTime.parse("2026-10-01T09:00:00Z"));
        entity.setFechaActualizacion(OffsetDateTime.parse("2026-10-01T09:00:00Z"));
        when(entityManager.find(ReservaEntity.class, bookingId)).thenReturn(entity);

        // Act
        Optional<Booking> result = adapter.findById(bookingId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(bookingId);
        assertThat(result.get().getAvailabilityId()).isEqualTo(entity.getDisponibilidadId());
        assertThat(result.get().getEstadoReservaId()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyWhenBookingDoesNotExist() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        UUID bookingId = UUID.randomUUID();
        when(entityManager.find(ReservaEntity.class, bookingId)).thenReturn(null);

        // Act
        Optional<Booking> result = adapter.findById(bookingId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAvailabilityIdByBookingIdUsingQuery() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        TypedQuery<UUID> query = mock(TypedQuery.class);
        UUID bookingId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        when(entityManager.createQuery(anyString(), eq(UUID.class))).thenReturn(query);
        when(query.setParameter("id", bookingId)).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(availabilityId));

        // Act
        Optional<UUID> result = adapter.findAvailabilityIdByBookingId(bookingId);

        // Assert
        assertThat(result).contains(availabilityId);
    }

    @Test
    void shouldCreateOccupationAndPersistEntity() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        UUID availabilityId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        // Act
        adapter.createOccupation(availabilityId, bookingId);

        // Assert
        ArgumentCaptor<OcupacionDisponibilidadEntity> captor = ArgumentCaptor.forClass(OcupacionDisponibilidadEntity.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getDisponibilidadId()).isEqualTo(availabilityId);
        assertThat(captor.getValue().getReservaId()).isEqualTo(bookingId);
        assertThat(captor.getValue().getFechaOcupacion()).isNotNull();
    }

    @Test
    void shouldDeleteOccupationUsingBulkDeleteQuery() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        Query query = mock(Query.class);
        UUID availabilityId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("a", availabilityId)).thenReturn(query);
        when(query.setParameter("b", bookingId)).thenReturn(query);

        // Act
        adapter.deleteOccupation(availabilityId, bookingId);

        // Assert
        verify(query).executeUpdate();
    }

    @Test
    void shouldSaveStatusHistoryEntity() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        BookingStatusHistory history = new BookingStatusHistory(
                UUID.randomUUID(),
                10,
                11,
                UUID.randomUUID(),
                "Cambio solicitado",
                OffsetDateTime.parse("2026-10-12T09:00:00Z")
        );

        // Act
        adapter.saveStatusHistory(history);

        // Assert
        ArgumentCaptor<HistorialEstadoReservaEntity> captor = ArgumentCaptor.forClass(HistorialEstadoReservaEntity.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getReservaId()).isEqualTo(history.reservaId());
        assertThat(captor.getValue().getEstadoAnteriorId()).isEqualTo(history.estadoAnteriorId());
        assertThat(captor.getValue().getEstadoNuevoId()).isEqualTo(history.estadoNuevoId());
    }

    @Test
    void shouldResolveReservationStatusByCodeCaseInsensitive() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        TypedQuery<Integer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Integer.class))).thenReturn(query);
        when(query.setParameter("c", "CONFIRMADA")).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(10));

        // Act
        Integer result = adapter.findReservationStatusIdByCode("confirmada");

        // Assert
        assertThat(result).isEqualTo(10);
    }

    @Test
    void shouldReturnNullWhenReservationStatusCodeIsUnknown() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(entityManager);
        TypedQuery<Integer> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Integer.class))).thenReturn(query);
        when(query.setParameter("c", "UNKNOWN")).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.empty());

        // Act
        Integer result = adapter.findReservationStatusIdByCode("unknown");

        // Assert
        assertThat(result).isNull();
    }

    private Booking bookingWithoutId() {
        Booking booking = new Booking();
        booking.setClienteId(UUID.randomUUID());
        booking.setAvailabilityId(UUID.randomUUID());
        booking.setEstadoReservaId(10);
        booking.setFechaReserva(OffsetDateTime.parse("2026-10-10T09:00:00Z"));
        booking.setNotas("Notas");
        booking.setFechaCreacion(OffsetDateTime.parse("2026-10-01T09:00:00Z"));
        booking.setFechaActualizacion(OffsetDateTime.parse("2026-10-01T09:00:00Z"));
        return booking;
    }
}
