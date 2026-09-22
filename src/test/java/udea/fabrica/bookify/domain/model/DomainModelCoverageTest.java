package udea.fabrica.bookify.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelCoverageTest {

    @Test
    void shouldReadAndWriteAvailabilityFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID agendaId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime created = OffsetDateTime.parse("2026-10-01T10:00:00Z");
        Availability availability = new Availability();

        // Act
        availability.setId(id);
        availability.setAgendaServicioId(agendaId);
        availability.setEstadoDisponibilidadId(1);
        availability.setInicioAt(OffsetDateTime.parse("2026-10-15T08:00:00Z"));
        availability.setFinAt(OffsetDateTime.parse("2026-10-15T09:00:00Z"));
        availability.setCreadoPorUsuarioId(userId);
        availability.setFechaCreacion(created);
        availability.setFechaActualizacion(created);
        Availability constructed = new Availability(
                id, agendaId, 1, availability.getInicioAt(), availability.getFinAt(), userId, created, created
        );

        // Assert
        assertThat(availability.getId()).isEqualTo(id);
        assertThat(availability.getAgendaServicioId()).isEqualTo(agendaId);
        assertThat(availability.getEstadoDisponibilidadId()).isEqualTo(1);
        assertThat(constructed.getFechaCreacion()).isEqualTo(created);
    }

    @Test
    void shouldUseAvailableScheduleAndServiceOfferHelpers() {
        // Arrange
        UUID availabilityId = UUID.randomUUID();
        AvailableSchedule schedule = new AvailableSchedule();
        ServiceOffer offer = new ServiceOffer();

        // Act
        schedule.setAvailabilityId(availabilityId);
        schedule.setInicioAt(OffsetDateTime.parse("2026-10-15T10:00:00Z"));
        schedule.setFinAt(OffsetDateTime.parse("2026-10-15T10:30:00Z"));
        offer.setServiceId(UUID.randomUUID());
        offer.setService("Corte");
        offer.setProvider("Barberia");
        offer.setAvailableSchedules(new ArrayList<>(List.of(schedule)));
        offer.addAvailableSchedule(new AvailableSchedule(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-10-15T11:00:00Z"),
                OffsetDateTime.parse("2026-10-15T11:30:00Z")
        ));
        ServiceOffer constructed = new ServiceOffer(offer.getServiceId(), "Corte", "Barberia", List.of(schedule));

        // Assert
        assertThat(schedule.getAvailabilityId()).isEqualTo(availabilityId);
        assertThat(offer.getAvailableSchedules()).hasSize(2);
        assertThat(constructed.getService()).isEqualTo("Corte");
    }

    @Test
    void shouldExposeBookingAndHistoryFields() {
        // Arrange
        Booking booking = new Booking();
        BookingHistory history = new BookingHistory("Masaje", "Spa", LocalDate.parse("2026-10-15"), LocalTime.parse("16:00"), "CONFIRMADA");

        // Act
        booking.setId(UUID.randomUUID());
        booking.setClienteId(UUID.randomUUID());
        booking.setAvailabilityId(UUID.randomUUID());
        booking.setEstadoReservaId(10);
        booking.setFechaReserva(OffsetDateTime.parse("2026-10-15T16:00:00Z"));
        booking.setNotas("Nota");
        booking.setFechaCreacion(OffsetDateTime.parse("2026-10-01T16:00:00Z"));
        booking.setFechaActualizacion(OffsetDateTime.parse("2026-10-01T16:00:00Z"));

        // Assert
        assertThat(booking.getAvailabilityId()).isNotNull();
        assertThat(booking.getNotas()).isEqualTo("Nota");
        assertThat(history.getService()).isEqualTo("Masaje");
        assertThat(history.getHora()).isEqualTo(LocalTime.parse("16:00"));
    }
}
