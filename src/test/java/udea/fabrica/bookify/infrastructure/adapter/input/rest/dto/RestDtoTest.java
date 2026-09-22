package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.BookingRequestDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RestDtoTest {

    @Test
    void shouldReadAndWriteRequestDtos() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        AvailabilityRequestDto availabilityRequestDto = new AvailabilityRequestDto();
        UUID agendaId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-10-13T08:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-10-13T09:00:00Z");

        // Act
        bookingRequestDto.setClienteId(clientId);
        bookingRequestDto.setDisponibilidadId(availabilityId);
        bookingRequestDto.setNotas("Nota");
        availabilityRequestDto.setAgendaServicioId(agendaId);
        availabilityRequestDto.setInicioAt(start);
        availabilityRequestDto.setFinAt(end);
        availabilityRequestDto.setCreadoPorUsuarioId(clientId);

        // Assert
        assertThat(bookingRequestDto.getClienteId()).isEqualTo(clientId);
        assertThat(bookingRequestDto.getDisponibilidadId()).isEqualTo(availabilityId);
        assertThat(bookingRequestDto.getNotas()).isEqualTo("Nota");
        assertThat(availabilityRequestDto.getAgendaServicioId()).isEqualTo(agendaId);
        assertThat(availabilityRequestDto.getInicioAt()).isEqualTo(start);
        assertThat(availabilityRequestDto.getFinAt()).isEqualTo(end);
    }

    @Test
    void shouldBuildAndReadResponseDtos() {
        // Arrange
        UUID id = UUID.randomUUID();
        var schedule = AvailableScheduleResponseDto.builder()
                .availabilityId(UUID.randomUUID())
                .inicioAt(OffsetDateTime.parse("2026-10-13T10:00:00Z"))
                .finAt(OffsetDateTime.parse("2026-10-13T10:30:00Z"))
                .build();

        // Act
        var booking = BookingResponseDto.builder()
                .id(id)
                .clienteId(UUID.randomUUID())
                .disponibilidadId(UUID.randomUUID())
                .estadoReservaId(10)
                .fechaReserva(OffsetDateTime.parse("2026-10-13T10:00:00Z"))
                .notas("Test")
                .build();
        var availability = AvailabilityResponseDto.builder()
                .id(UUID.randomUUID())
                .agendaServicioId(UUID.randomUUID())
                .estadoDisponibilidadId(1)
                .inicioAt(OffsetDateTime.parse("2026-10-13T10:00:00Z"))
                .finAt(OffsetDateTime.parse("2026-10-13T11:00:00Z"))
                .creadoPorUsuarioId(UUID.randomUUID())
                .fechaCreacion(OffsetDateTime.parse("2026-10-13T09:00:00Z"))
                .build();
        var historyRow = BookingHistoryResponseDto.builder()
                .servicio("Servicio")
                .proveedor("Proveedor")
                .fecha(LocalDate.parse("2026-10-13"))
                .hora(LocalTime.parse("10:00"))
                .estado("CONFIRMADA")
                .build();
        var history = BookingHistoryListResponseDto.builder()
                .message(null)
                .reservas(List.of(historyRow))
                .build();
        var offer = ServiceOfferResponseDto.builder()
                .serviceId(UUID.randomUUID())
                .service("Corte")
                .provider("Barberia")
                .availableSchedules(List.of(schedule))
                .build();

        // Assert
        assertThat(booking.getId()).isEqualTo(id);
        assertThat(booking.getEstadoReservaId()).isEqualTo(10);
        assertThat(availability.getEstadoDisponibilidadId()).isEqualTo(1);
        assertThat(history.getReservas()).hasSize(1);
        assertThat(history.getReservas().get(0).getServicio()).isEqualTo("Servicio");
        assertThat(offer.getAvailableSchedules()).hasSize(1);
    }
}
