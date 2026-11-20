package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.exception.AvailabilityNotFoundException;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.exception.DomainForbiddenException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;
import udea.fabrica.bookify.domain.port.out.ProviderAvailabilityPort;

@ExtendWith(MockitoExtension.class)
class ManageAvailabilityUseCaseImplTest {

    private static final OffsetDateTime START = OffsetDateTime.parse("2026-11-20T09:00:00-05:00");
    private static final OffsetDateTime END = OffsetDateTime.parse("2026-11-20T10:00:00-05:00");

    @Mock
    private ProviderAvailabilityPort availabilityPort;

    @Mock
    private MerchantProfileRepositoryPort merchantProfileRepositoryPort;

    @InjectMocks
    private ManageAvailabilityUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID providerId = UUID.randomUUID();
    private final UUID availabilityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(merchantProfileRepositoryPort.findByUserId(userId))
                .thenReturn(Optional.of(MerchantProfile.builder().id(providerId).userId(userId).build()));
    }

    @Test
    void shouldThrowForbidden_whenRoleIsCliente() {
        assertThatThrownBy(() -> useCase.update(userId, "CLIENTE", availabilityId, START, END))
                .isInstanceOf(DomainForbiddenException.class);
        verifyNoInteractions(availabilityPort);
    }

    @Test
    void shouldThrowBadRequest_whenEndIsNotAfterStart() {
        assertThatThrownBy(() -> useCase.update(userId, "PROVEEDOR", availabilityId, END, START))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor");
        verifyNoInteractions(availabilityPort);
    }

    @Test
    void shouldThrowNotFound_whenAvailabilityBelongsToAnotherBusiness() {
        when(availabilityPort.lockOwnedAvailability(availabilityId, providerId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.update(userId, "PROVEEDOR", availabilityId, START, END))
                .isInstanceOf(AvailabilityNotFoundException.class);
        verify(availabilityPort, never()).updateRange(availabilityId, START, END);
    }

    @Test
    void shouldThrowConflict_whenAvailabilityHasConfirmedBookingsOnUpdate() {
        when(availabilityPort.lockOwnedAvailability(availabilityId, providerId)).thenReturn(true);
        when(availabilityPort.countConfirmedBookings(availabilityId)).thenReturn(2);

        assertThatThrownBy(() -> useCase.update(userId, "PROVEEDOR", availabilityId, START, END))
                .isInstanceOf(DomainConflictException.class)
                .hasMessageContaining("2 reserva");
        verify(availabilityPort, never()).updateRange(availabilityId, START, END);
    }

    @Test
    void shouldThrowOverlap_whenNewRangeCollidesWithAnotherAvailability() {
        when(availabilityPort.lockOwnedAvailability(availabilityId, providerId)).thenReturn(true);
        when(availabilityPort.countConfirmedBookings(availabilityId)).thenReturn(0);
        when(availabilityPort.existsOverlappingExcluding(availabilityId, START, END)).thenReturn(true);

        assertThatThrownBy(() -> useCase.update(userId, "PROVEEDOR", availabilityId, START, END))
                .isInstanceOf(AvailabilityOverlapException.class);
    }

    @Test
    void shouldUpdateRange_whenThereAreNoConfirmedBookings() {
        Availability updated = new Availability(availabilityId, UUID.randomUUID(), 1, START, END, null,
                START, START);
        when(availabilityPort.lockOwnedAvailability(availabilityId, providerId)).thenReturn(true);
        when(availabilityPort.countConfirmedBookings(availabilityId)).thenReturn(0);
        when(availabilityPort.existsOverlappingExcluding(availabilityId, START, END)).thenReturn(false);
        when(availabilityPort.updateRange(availabilityId, START, END)).thenReturn(updated);

        assertThat(useCase.update(userId, "PROVEEDOR", availabilityId, START, END)).isSameAs(updated);
    }

    @Test
    void shouldThrowConflict_whenAvailabilityHasConfirmedBookingsOnDelete() {
        when(availabilityPort.lockOwnedAvailability(availabilityId, providerId)).thenReturn(true);
        when(availabilityPort.countConfirmedBookings(availabilityId)).thenReturn(1);

        assertThatThrownBy(() -> useCase.delete(userId, "PROVEEDOR", availabilityId))
                .isInstanceOf(DomainConflictException.class);
        verify(availabilityPort, never()).delete(availabilityId);
    }

    @Test
    void shouldDelete_whenThereAreNoConfirmedBookings() {
        when(availabilityPort.lockOwnedAvailability(availabilityId, providerId)).thenReturn(true);
        when(availabilityPort.countConfirmedBookings(availabilityId)).thenReturn(0);

        useCase.delete(userId, "PROVEEDOR", availabilityId);

        verify(availabilityPort).delete(availabilityId);
    }
}