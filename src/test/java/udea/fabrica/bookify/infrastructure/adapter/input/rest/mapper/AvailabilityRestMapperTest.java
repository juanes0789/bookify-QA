package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.AvailabilityRequestDto;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityRestMapperTest {

    @Test
    void shouldMapRequestDtoToDomainModel() {
        // Arrange
        UUID agendaId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-10-04T09:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-10-04T10:00:00Z");
        AvailabilityRequestDto dto = new AvailabilityRequestDto();
        dto.setAgendaServicioId(agendaId);
        dto.setInicioAt(start);
        dto.setFinAt(end);
        dto.setCreadoPorUsuarioId(userId);

        // Act
        Availability result = AvailabilityRestMapper.toDomain(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAgendaServicioId()).isEqualTo(agendaId);
        assertThat(result.getInicioAt()).isEqualTo(start);
        assertThat(result.getFinAt()).isEqualTo(end);
        assertThat(result.getCreadoPorUsuarioId()).isEqualTo(userId);
    }

    @Test
    void shouldMapDomainModelToResponseDto() {
        // Arrange
        Availability domain = new Availability();
        domain.setId(UUID.randomUUID());
        domain.setAgendaServicioId(UUID.randomUUID());
        domain.setEstadoDisponibilidadId(1);
        domain.setInicioAt(OffsetDateTime.parse("2026-10-04T09:00:00Z"));
        domain.setFinAt(OffsetDateTime.parse("2026-10-04T10:00:00Z"));
        domain.setCreadoPorUsuarioId(UUID.randomUUID());
        domain.setFechaCreacion(OffsetDateTime.parse("2026-10-01T09:00:00Z"));

        // Act
        var result = AvailabilityRestMapper.toResponse(domain);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(domain.getId());
        assertThat(result.getAgendaServicioId()).isEqualTo(domain.getAgendaServicioId());
        assertThat(result.getEstadoDisponibilidadId()).isEqualTo(1);
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        // Arrange / Act
        Availability fromRequest = AvailabilityRestMapper.toDomain(null);
        var fromDomain = AvailabilityRestMapper.toResponse(null);

        // Assert
        assertThat(fromRequest).isNull();
        assertThat(fromDomain).isNull();
    }
}
