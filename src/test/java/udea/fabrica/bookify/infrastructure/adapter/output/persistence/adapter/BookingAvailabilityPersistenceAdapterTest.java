package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoDisponibilidadEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AvailabilityJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.EstadoDisponibilidadJpaRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingAvailabilityPersistenceAdapterTest {

    @Test
    void shouldFindAvailabilityByIdForUpdate() {
        // Arrange
        AvailabilityJpaRepository availabilityRepository = mock(AvailabilityJpaRepository.class);
        EstadoDisponibilidadJpaRepository estadoRepository = mock(EstadoDisponibilidadJpaRepository.class);
        BookingAvailabilityPersistenceAdapter adapter = new BookingAvailabilityPersistenceAdapter(availabilityRepository, estadoRepository);
        UUID id = UUID.randomUUID();
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setId(id);
        when(availabilityRepository.findByIdForUpdate(id)).thenReturn(Optional.of(entity));

        // Act
        Optional<Availability> result = adapter.findByIdForUpdate(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void shouldSaveAvailabilityAndResolveStatusId() throws Exception {
        // Arrange
        AvailabilityJpaRepository availabilityRepository = mock(AvailabilityJpaRepository.class);
        EstadoDisponibilidadJpaRepository estadoRepository = mock(EstadoDisponibilidadJpaRepository.class);
        BookingAvailabilityPersistenceAdapter adapter = new BookingAvailabilityPersistenceAdapter(availabilityRepository, estadoRepository);

        Availability availability = new Availability();
        availability.setId(UUID.randomUUID());
        availability.setAgendaServicioId(UUID.randomUUID());
        availability.setEstadoDisponibilidadId(2);
        AvailabilityEntity saved = new AvailabilityEntity();
        saved.setId(availability.getId());
        saved.setAgendaServicioId(availability.getAgendaServicioId());
        saved.setEstadoDisponibilidadId(2);
        when(availabilityRepository.save(any(AvailabilityEntity.class))).thenReturn(saved);

        EstadoDisponibilidadEntity estado = new EstadoDisponibilidadEntity();
        Field id = EstadoDisponibilidadEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(estado, 5);
        when(estadoRepository.findByCodigo("DISPONIBLE")).thenReturn(Optional.of(estado));

        // Act
        Availability persisted = adapter.save(availability);
        Integer statusId = adapter.findAvailabilityStatusIdByCode("DISPONIBLE");

        // Assert
        assertThat(persisted.getId()).isEqualTo(availability.getId());
        assertThat(statusId).isEqualTo(5);
    }
}
