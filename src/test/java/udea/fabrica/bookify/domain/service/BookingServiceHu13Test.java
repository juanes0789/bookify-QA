package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.exception.ResourceUnavailableException;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.port.out.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceHu13Test {
    @Mock private BookingRepositoryPort bookings;
    @Mock private AvailabilityRepositoryPort availability;
    @Mock private ResourceAvailabilityPort resources;
    @InjectMocks private BookingService service;

    @Test
    void shouldAssignAvailableResource_whenCreatingResourceScheduledBooking() {
        // Arrange
        Booking booking = resourceBooking();
        UUID resourceId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(resources.findAvailableResource(booking.getProviderId(), booking.getStartAt(), booking.getEndAt())).thenReturn(Optional.of(resourceId));
        when(bookings.findReservationStatusIdByCode("CONFIRMADA")).thenReturn(10);
        when(bookings.save(any())).thenAnswer(invocation -> { Booking saved = invocation.getArgument(0); saved.setId(bookingId); return saved; });

        // Act
        Booking saved = service.create(booking);

        // Assert
        assertThat(saved.getResourceId()).isEqualTo(resourceId);
        assertThat(saved.getEstadoReservaId()).isEqualTo(10);
        verify(resources).assignResourceToBooking(bookingId, resourceId);
        verify(bookings).save(booking);
        verifyNoInteractions(availability);
    }

    @Test
    void shouldNotSaveBooking_whenNoResourceIsAvailable() {
        // Arrange
        Booking booking = resourceBooking();
        when(resources.findAvailableResource(any(), any(), any())).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> service.create(booking)).isInstanceOf(ResourceUnavailableException.class);
        verify(bookings, never()).save(any());
        verify(resources, never()).assignResourceToBooking(any(), any());
    }

    private Booking resourceBooking() {
        OffsetDateTime start = OffsetDateTime.now().plusHours(1);
        Booking booking = new Booking();
        booking.setClienteId(UUID.randomUUID());
        booking.setProviderId(UUID.randomUUID());
        booking.setServiceId(UUID.randomUUID());
        booking.setStartAt(start);
        booking.setEndAt(start.plusHours(1));
        return booking;
    }
}
