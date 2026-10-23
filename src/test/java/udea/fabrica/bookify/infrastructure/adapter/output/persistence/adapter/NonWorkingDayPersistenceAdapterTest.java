package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.DiaNoLaborableEntity;

@ExtendWith(MockitoExtension.class)
class NonWorkingDayPersistenceAdapterTest {
    @Mock private EntityManager entityManager;
    @Mock private Query query;
    @InjectMocks private NonWorkingDayPersistenceAdapter adapter;

    @Test
    void shouldReturnAffectedBookingIds_whenDayIsQueried() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        List<UUID> bookingIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("providerId"), eq(providerId))).thenReturn(query);
        when(query.setParameter(eq("start"), any())).thenReturn(query);
        when(query.setParameter(eq("end"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(bookingIds);

        // Act
        List<UUID> result = adapter.findAffectedBookingIds(providerId, date);

        // Assert
        assertThat(result).containsExactlyElementsOf(bookingIds);
        verify(query).setParameter("providerId", providerId);
        ArgumentCaptor<OffsetDateTime> start = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> end = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(query).setParameter(eq("start"), start.capture());
        verify(query).setParameter(eq("end"), end.capture());
        assertThat(start.getValue()).isEqualTo(date.atStartOfDay().atOffset(java.time.ZoneOffset.ofHours(-5)));
        assertThat(end.getValue()).isEqualTo(date.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.ofHours(-5)));
        verify(query).getResultList();
    }

    @Test
    void shouldMapDomainDayToEntity_whenSaving() {
        // Arrange
        UUID generatedId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NonWorkingDay day = new NonWorkingDay(null, providerId, LocalDate.now().plusDays(1),
                "Mantenimiento", userId, OffsetDateTime.now());

        doAnswer(invocation -> {
            DiaNoLaborableEntity entity = invocation.getArgument(0);
            entity.setId(generatedId); // Simula la asignación de ID por parte de Hibernate/JPA
            return null;
        }).when(entityManager).persist(any(DiaNoLaborableEntity.class));

        // Act
        NonWorkingDay saved = adapter.save(day);

        // Assert: verify persist() fue llamado y los campos funcionales son correctos
        ArgumentCaptor<DiaNoLaborableEntity> captor = ArgumentCaptor.forClass(DiaNoLaborableEntity.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getProviderId()).isEqualTo(providerId);
        assertThat(captor.getValue().getCreatedByUserId()).isEqualTo(userId);
        // El ID generado por Hibernate se propaga de vuelta al objeto de dominio
        assertThat(saved.getId()).isEqualTo(generatedId);
    }

    @Test
    void shouldReturnTrue_whenProviderAlreadyRegisteredTheDate() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 12, 25);
        stubCountQuery(providerId, date, 1L);

        // Act
        boolean exists = adapter.existsByProveedorIdAndFecha(providerId, date);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenDateIsNotMarkedAsNonWorkingDay() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 12, 24);
        stubCountQuery(providerId, date, 0L);

        // Act
        boolean nonWorkingDay = adapter.isNonWorkingDay(providerId, date);

        // Assert
        assertThat(nonWorkingDay).isFalse();
    }

    @Test
    void shouldReturnProviderId_whenAgendaExists() {
        // Arrange
        UUID agendaId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("agendaId", agendaId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(providerId);

        // Act
        UUID result = adapter.findProviderIdByAgendaServicioId(agendaId);

        // Assert
        assertThat(result).isEqualTo(providerId);
    }

    @Test
    void shouldReturnNull_whenAgendaDoesNotExist() {
        // Arrange
        UUID agendaId = UUID.randomUUID();
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("agendaId", agendaId)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException("Sin agenda"));

        // Act
        UUID result = adapter.findProviderIdByAgendaServicioId(agendaId);

        // Assert
        assertThat(result).isNull();
    }

    private void stubCountQuery(UUID providerId, LocalDate date, long count) {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("proveedorId", providerId)).thenReturn(query);
        when(query.setParameter("fecha", date)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(count);
    }
}
