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
    void shouldSetCreatedAt_andDelegateIdGeneration_whenConfirmingDayWithoutThem() {
        // La generación del ID es responsabilidad de la capa de persistencia (@GeneratedValue en Hibernate).
        // El servicio pasa id=null y garantiza que fechaCreacion no sea null.
        // Arrange
        UUID persistedId = UUID.randomUUID();
        NonWorkingDay requested = new NonWorkingDay(null, UUID.randomUUID(), LocalDate.now().plusDays(1),
                "Inventario", UUID.randomUUID(), null);
        // Simula que la capa de persistencia asigna el ID generado
        when(days.save(any())).thenAnswer(invocation -> {
            NonWorkingDay arg = invocation.getArgument(0);
            arg.setId(persistedId);
            return arg;
        });

        // Act
        NonWorkingDay saved = service.confirmar(requested);

        // Assert: el servicio garantiza que fechaCreacion no sea null al llamar a save()
        verify(days).save(argThat(day -> day.getFechaCreacion() != null));
        // El ID asignado por la capa de persistencia llega al resultado final
        assertThat(saved.getId()).isEqualTo(persistedId);
        assertThat(saved.getFechaCreacion()).isBeforeOrEqualTo(OffsetDateTime.now());
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
