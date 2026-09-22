package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityEntityMapperTest {

    @Test
    void shouldMapDomainToEntity() {
        // Arrange
        Availability domain = new Availability(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                OffsetDateTime.parse("2026-10-07T10:00:00Z"),
                OffsetDateTime.parse("2026-10-07T11:00:00Z"),
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-10-01T10:00:00Z"),
                OffsetDateTime.parse("2026-10-01T10:00:00Z")
        );

        // Act
        AvailabilityEntity entity = AvailabilityEntityMapper.toEntity(domain);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getAgendaServicioId()).isEqualTo(domain.getAgendaServicioId());
        assertThat(entity.getEstadoDisponibilidadId()).isEqualTo(1);
    }

    @Test
    void shouldMapEntityToDomain() {
        // Arrange
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setId(UUID.randomUUID());
        entity.setAgendaServicioId(UUID.randomUUID());
        entity.setEstadoDisponibilidadId(2);
        entity.setInicioAt(OffsetDateTime.parse("2026-10-07T12:00:00Z"));
        entity.setFinAt(OffsetDateTime.parse("2026-10-07T13:00:00Z"));
        entity.setCreadoPorUsuarioId(UUID.randomUUID());
        entity.setFechaCreacion(OffsetDateTime.parse("2026-10-01T12:00:00Z"));
        entity.setFechaActualizacion(OffsetDateTime.parse("2026-10-01T12:00:00Z"));

        // Act
        Availability domain = AvailabilityEntityMapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getAgendaServicioId()).isEqualTo(entity.getAgendaServicioId());
        assertThat(domain.getEstadoDisponibilidadId()).isEqualTo(2);
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        // Arrange / Act
        AvailabilityEntity entity = AvailabilityEntityMapper.toEntity(null);
        Availability domain = AvailabilityEntityMapper.toDomain(null);

        // Assert
        assertThat(entity).isNull();
        assertThat(domain).isNull();
    }
}
