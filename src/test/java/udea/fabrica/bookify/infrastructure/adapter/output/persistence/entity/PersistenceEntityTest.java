package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceEntityTest {

    @Test
    void shouldReadAndWriteAvailabilityAndReservaEntities() {
        // Arrange
        AvailabilityEntity availability = new AvailabilityEntity();
        ReservaEntity reserva = new ReservaEntity();
        UUID id = UUID.randomUUID();

        // Act
        availability.setId(id);
        availability.setAgendaServicioId(UUID.randomUUID());
        availability.setEstadoDisponibilidadId(1);
        availability.setInicioAt(OffsetDateTime.parse("2026-10-14T10:00:00Z"));
        availability.setFinAt(OffsetDateTime.parse("2026-10-14T11:00:00Z"));
        reserva.setId(UUID.randomUUID());
        reserva.setClienteId(UUID.randomUUID());
        reserva.setEstadoReservaId(10);
        reserva.setDisponibilidadId(id);
        reserva.setFechaReserva(OffsetDateTime.parse("2026-10-14T10:00:00Z"));

        // Assert
        assertThat(availability.getId()).isEqualTo(id);
        assertThat(availability.getEstadoDisponibilidadId()).isEqualTo(1);
        assertThat(reserva.getDisponibilidadId()).isEqualTo(id);
        assertThat(reserva.getEstadoReservaId()).isEqualTo(10);
    }

    @Test
    void shouldReadEstadoDisponibilidadIdGetter() throws Exception {
        // Arrange
        EstadoDisponibilidadEntity estado = new EstadoDisponibilidadEntity();
        Field idField = EstadoDisponibilidadEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(estado, 4);

        // Act
        Integer id = estado.getId();

        // Assert
        assertThat(id).isEqualTo(4);
    }

    @Test
    void shouldHandleOtherEntityClasses() {
        // Arrange
        HistorialEstadoReservaEntity history = new HistorialEstadoReservaEntity();
        OcupacionDisponibilidadEntity ocupacion = new OcupacionDisponibilidadEntity();
        EstadoReservaEntity estadoReserva = new EstadoReservaEntity();
        OcupacionDisponibilidadEntity.Key key = new OcupacionDisponibilidadEntity.Key(UUID.randomUUID(), UUID.randomUUID());
        AgendaServicioEntity agenda = new AgendaServicioEntity();

        // Act
        history.setReservaId(UUID.randomUUID());
        history.setEstadoAnteriorId(10);
        history.setEstadoNuevoId(11);
        history.setFechaCambio(OffsetDateTime.parse("2026-10-14T12:00:00Z"));
        ocupacion.setDisponibilidadId(UUID.randomUUID());
        ocupacion.setReservaId(UUID.randomUUID());
        ocupacion.setFechaOcupacion(OffsetDateTime.parse("2026-10-14T12:00:00Z"));

        // Assert
        assertThat(history.getEstadoNuevoId()).isEqualTo(11);
        assertThat(ocupacion.getFechaOcupacion()).isNotNull();
        assertThat(key).isNotNull();
        assertThat(estadoReserva).isNotNull();
        assertThat(agenda).isNotNull();
    }
}
