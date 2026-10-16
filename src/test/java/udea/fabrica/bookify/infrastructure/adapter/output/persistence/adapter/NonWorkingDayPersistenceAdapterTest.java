package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
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
        verify(query).getResultList();
    }

    @Test
    void shouldMapDomainDayToEntity_whenSaving() {
        // Arrange
        NonWorkingDay day = new NonWorkingDay(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1),
                "Mantenimiento", UUID.randomUUID(), OffsetDateTime.now());

        // Act
        NonWorkingDay saved = adapter.save(day);

        // Assert
        ArgumentCaptor<DiaNoLaborableEntity> captor = ArgumentCaptor.forClass(DiaNoLaborableEntity.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(day.id());
        assertThat(captor.getValue().getProviderId()).isEqualTo(day.providerId());
        assertThat(captor.getValue().getCreatedByUserId()).isEqualTo(day.createdByUserId());
        assertThat(saved).isEqualTo(day);
    }
}
