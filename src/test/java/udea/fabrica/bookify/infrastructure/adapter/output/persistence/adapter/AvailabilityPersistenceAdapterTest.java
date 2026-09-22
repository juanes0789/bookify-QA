package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoDisponibilidadEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AgendaServicioJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AvailabilityJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.EstadoDisponibilidadJpaRepository;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AvailabilityPersistenceAdapterTest {

    @Test
    void shouldSaveAvailabilityThroughRepository() {
        // Arrange
        AvailabilityJpaRepository repository = mock(AvailabilityJpaRepository.class);
        AgendaServicioJpaRepository agendaRepository = mock(AgendaServicioJpaRepository.class);
        EstadoDisponibilidadJpaRepository estadoRepository = mock(EstadoDisponibilidadJpaRepository.class);
        AvailabilityPersistenceAdapter adapter = new AvailabilityPersistenceAdapter(repository, agendaRepository, estadoRepository);

        Availability input = new Availability();
        input.setAgendaServicioId(UUID.randomUUID());
        input.setEstadoDisponibilidadId(1);
        input.setInicioAt(OffsetDateTime.parse("2026-10-08T10:00:00Z"));
        input.setFinAt(OffsetDateTime.parse("2026-10-08T11:00:00Z"));

        AvailabilityEntity savedEntity = new AvailabilityEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setAgendaServicioId(input.getAgendaServicioId());
        savedEntity.setEstadoDisponibilidadId(1);
        savedEntity.setInicioAt(input.getInicioAt());
        savedEntity.setFinAt(input.getFinAt());
        when(repository.save(any(AvailabilityEntity.class))).thenReturn(savedEntity);

        // Act
        Availability result = adapter.save(input);

        // Assert
        verify(repository).save(any(AvailabilityEntity.class));
        assertThat(result.getId()).isEqualTo(savedEntity.getId());
        assertThat(result.getAgendaServicioId()).isEqualTo(input.getAgendaServicioId());
    }

    @Test
    void shouldDelegateOverlapAndActiveAgendaChecks() {
        // Arrange
        AvailabilityJpaRepository repository = mock(AvailabilityJpaRepository.class);
        AgendaServicioJpaRepository agendaRepository = mock(AgendaServicioJpaRepository.class);
        EstadoDisponibilidadJpaRepository estadoRepository = mock(EstadoDisponibilidadJpaRepository.class);
        AvailabilityPersistenceAdapter adapter = new AvailabilityPersistenceAdapter(repository, agendaRepository, estadoRepository);
        UUID agendaId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-10-08T10:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-10-08T11:00:00Z");
        when(repository.existsOverlappingAvailability(agendaId, start, end)).thenReturn(true);
        when(agendaRepository.existsByIdAndActivoTrue(agendaId)).thenReturn(true);

        // Act
        boolean overlaps = adapter.existsOverlapping(agendaId, start, end);
        boolean active = adapter.existsActiveAgendaServicio(agendaId);

        // Assert
        assertThat(overlaps).isTrue();
        assertThat(active).isTrue();
    }

    @Test
    void shouldResolveAvailabilityStatusByCodeOrReturnNull() throws Exception {
        // Arrange
        AvailabilityJpaRepository repository = mock(AvailabilityJpaRepository.class);
        AgendaServicioJpaRepository agendaRepository = mock(AgendaServicioJpaRepository.class);
        EstadoDisponibilidadJpaRepository estadoRepository = mock(EstadoDisponibilidadJpaRepository.class);
        AvailabilityPersistenceAdapter adapter = new AvailabilityPersistenceAdapter(repository, agendaRepository, estadoRepository);
        EstadoDisponibilidadEntity estado = new EstadoDisponibilidadEntity();
        Field id = EstadoDisponibilidadEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(estado, 7);
        when(estadoRepository.findByCodigo("DISPONIBLE")).thenReturn(Optional.of(estado));
        when(estadoRepository.findByCodigo("MISSING")).thenReturn(Optional.empty());

        // Act
        Integer found = adapter.findAvailabilityStatusIdByCode("DISPONIBLE");
        Integer missing = adapter.findAvailabilityStatusIdByCode("MISSING");

        // Assert
        assertThat(found).isEqualTo(7);
        assertThat(missing).isNull();
    }
}
