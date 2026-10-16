package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ReservaRecursoEntity;

@ExtendWith(MockitoExtension.class)
class ResourceAvailabilityPersistenceAdapterTest {
    @Mock private EntityManager entityManager;
    @Mock private TypedQuery<UUID> query;
    @InjectMocks private ResourceAvailabilityPersistenceAdapter adapter;

    @Test
    void shouldReturnFreeResource_whenQueryFindsOne() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.now().plusHours(1);
        OffsetDateTime end = start.plusHours(1);
        when(entityManager.createQuery(anyString(), eq(UUID.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(resourceId));

        // Act
        var result = adapter.findAvailableResource(providerId, start, end);

        // Assert
        assertThat(result).contains(resourceId);
        verify(query).setParameter("idProveedor", providerId);
        verify(query).setMaxResults(1);
    }

    @Test
    void shouldPersistResourceAssignment_whenBookingAndResourceAreGiven() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        // Act
        adapter.assignResourceToBooking(bookingId, resourceId);

        // Assert
        ArgumentCaptor<ReservaRecursoEntity> captor = ArgumentCaptor.forClass(ReservaRecursoEntity.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getReservaId()).isEqualTo(bookingId);
        assertThat(captor.getValue().getRecursoId()).isEqualTo(resourceId);
    }
}
