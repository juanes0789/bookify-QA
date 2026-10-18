package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.exception.NonWorkingDayConflictException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CreateAvailabilityNonWorkingDayTest {

    @Test
    void rejectsAvailabilityOnProviderNonWorkingDay() {
        AvailabilityOutputPort availabilityPort = mock(AvailabilityOutputPort.class);
        NonWorkingDayOutputPort nonWorkingDayPort = mock(NonWorkingDayOutputPort.class);
        UUID agendaId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        when(availabilityPort.existsActiveAgendaServicio(agendaId)).thenReturn(true);
        when(nonWorkingDayPort.findProviderIdByAgendaServicioId(agendaId)).thenReturn(providerId);
        when(nonWorkingDayPort.isNonWorkingDay(eq(providerId), any())).thenReturn(true);

        Availability availability = new Availability();
        availability.setAgendaServicioId(agendaId);
        availability.setInicioAt(OffsetDateTime.parse("2026-12-25T10:00:00-05:00"));
        availability.setFinAt(OffsetDateTime.parse("2026-12-25T10:30:00-05:00"));

        CreateAvailabilityService service = new CreateAvailabilityService(availabilityPort, nonWorkingDayPort);

        assertThatThrownBy(() -> service.create(availability))
                .isInstanceOf(NonWorkingDayConflictException.class)
                .hasMessage("La fecha está marcada como no laborable");
        verify(availabilityPort, never()).save(any());
    }
}
