package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.model.*;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayPort;

@ExtendWith(MockitoExtension.class)
class BlockingDateServiceTest {
    @Mock private NonWorkingDayPort days;
    @InjectMocks private BlockingDateService service;

    @Test
    void shouldReturnAffectedBookingsCount_whenPreviewingBlockedDate() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        when(days.findAffectedBookingIds(providerId, date)).thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

        // Act
        BlockingPreview preview = service.previsualizar(providerId, date);

        // Assert
        assertThat(preview.affectedBookingsCount()).isEqualTo(2);
        verify(days).findAffectedBookingIds(providerId, date);
        verify(days, never()).save(any());
    }

    @Test
    void shouldGenerateIdAndCreatedAt_whenConfirmingDayWithoutThem() {
        // Arrange
        NonWorkingDay requested = new NonWorkingDay(null, UUID.randomUUID(), LocalDate.now().plusDays(1),
                "Inventario", UUID.randomUUID(), null);
        when(days.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        NonWorkingDay saved = service.confirmar(requested);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFechaCreacion()).isBeforeOrEqualTo(OffsetDateTime.now());
        verify(days).save(argThat(day -> day.getId() != null && day.getFechaCreacion() != null));
    }

    @Test
    void shouldNotSaveDay_whenProviderOrDateIsMissing() {
        // Arrange
        NonWorkingDay requested = new NonWorkingDay(UUID.randomUUID(), null, null, "Inventario", UUID.randomUUID(), OffsetDateTime.now());

        // Act / Assert
        assertThatThrownBy(() -> service.confirmar(requested)).isInstanceOf(IllegalArgumentException.class);
        verify(days, never()).save(any());
    }
}
