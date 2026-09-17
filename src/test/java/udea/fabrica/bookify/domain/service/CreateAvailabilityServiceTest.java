package udea.fabrica.bookify.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.exception.AvailabilityOverlapException;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAvailabilityServiceTest {

    @Mock
    private AvailabilityOutputPort availabilityOutputPort;

    @Test
    void shouldRegisterAvailableScheduleForAnActiveServiceAgenda() {
        UUID agendaServicioId = UUID.randomUUID();
        Availability availability = availability(agendaServicioId, OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1));
        when(availabilityOutputPort.existsActiveAgendaServicio(agendaServicioId)).thenReturn(true);
        when(availabilityOutputPort.existsOverlapping(any(), any(), any())).thenReturn(false);
        when(availabilityOutputPort.findAvailabilityStatusIdByCode("DISPONIBLE")).thenReturn(1);
        when(availabilityOutputPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Availability result = new CreateAvailabilityService(availabilityOutputPort).create(availability);

        assertThat(result.getEstadoDisponibilidadId()).isEqualTo(1);
        assertThat(result.getFechaCreacion()).isNotNull();
        assertThat(result.getFechaActualizacion()).isNotNull();
        ArgumentCaptor<Availability> saved = ArgumentCaptor.forClass(Availability.class);
        verify(availabilityOutputPort).save(saved.capture());
        assertThat(saved.getValue().getAgendaServicioId()).isEqualTo(agendaServicioId);
    }

    @Test
    void shouldRejectScheduleThatOverlapsAnExistingAvailability() {
        UUID agendaServicioId = UUID.randomUUID();
        Availability availability = availability(agendaServicioId, OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1));
        when(availabilityOutputPort.existsActiveAgendaServicio(agendaServicioId)).thenReturn(true);
        when(availabilityOutputPort.existsOverlapping(agendaServicioId, availability.getInicioAt(), availability.getFinAt())).thenReturn(true);

        assertThatThrownBy(() -> new CreateAvailabilityService(availabilityOutputPort).create(availability))
                .isInstanceOf(AvailabilityOverlapException.class)
                .hasMessage("El rango de horario ingresado se traslapa con un horario ya registrado.");

        verify(availabilityOutputPort, never()).save(any());
    }

    private Availability availability(UUID agendaServicioId, OffsetDateTime inicio, OffsetDateTime fin) {
        Availability availability = new Availability();
        availability.setAgendaServicioId(agendaServicioId);
        availability.setInicioAt(inicio);
        availability.setFinAt(fin);
        availability.setCreadoPorUsuarioId(UUID.randomUUID());
        return availability;
    }
}
