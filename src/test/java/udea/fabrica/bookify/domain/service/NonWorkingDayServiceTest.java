package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.exception.NonWorkingDayConflictException;
import udea.fabrica.bookify.domain.exception.PastDateException;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NonWorkingDayServiceTest {

    private NonWorkingDayOutputPort outputPort;
    private NonWorkingDayService service;
    private final UUID providerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        outputPort = mock(NonWorkingDayOutputPort.class);
        service = new NonWorkingDayService(outputPort);
        when(outputPort.save(any(NonWorkingDay.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registersFutureDateWithOptionalReason() {
        LocalDate date = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
        NonWorkingDay result = service.register(providerId, date, "Descanso");

        assertThat(result.getProveedorId()).isEqualTo(providerId);
        assertThat(result.getFecha()).isEqualTo(date);
        assertThat(result.getMotivo()).isEqualTo("Descanso");
        assertThat(result.getFechaCreacion()).isNotNull();
        verify(outputPort).save(any(NonWorkingDay.class));
    }

    @Test
    void allowsTodayAndNullReason() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Bogota"));
        assertThat(service.register(providerId, today, null).getFecha()).isEqualTo(today);
    }

    @Test
    void rejectsPastDate() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("America/Bogota")).minusDays(1);
        assertThatThrownBy(() -> service.register(providerId, yesterday, null))
                .isInstanceOf(PastDateException.class)
                .hasMessage("La fecha debe ser igual o posterior a hoy");
        verify(outputPort, never()).save(any());
    }

    @Test
    void rejectsDuplicateDate() {
        LocalDate date = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
        when(outputPort.existsByProveedorIdAndFecha(providerId, date)).thenReturn(true);

        assertThatThrownBy(() -> service.register(providerId, date, null))
                .isInstanceOf(NonWorkingDayConflictException.class)
                .hasMessage("La fecha ya está registrada como no laborable");
        verify(outputPort, never()).save(any());
    }

    @Test
    void rejectsReasonLongerThan100Characters() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
        String tooLongReason = "x".repeat(101);

        assertThatThrownBy(() -> service.register(providerId, tomorrow, tooLongReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El motivo no puede exceder 100 caracteres");
        verify(outputPort, never()).save(any());
    }
}
