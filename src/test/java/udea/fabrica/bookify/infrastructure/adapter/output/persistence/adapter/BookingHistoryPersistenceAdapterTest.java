package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.BookingHistoryJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.BookingHistoryProjection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingHistoryPersistenceAdapterTest {

    @Test
    void shouldMapProjectionRowsToBookingHistory() {
        // Arrange
        BookingHistoryJpaRepository repository = mock(BookingHistoryJpaRepository.class);
        BookingHistoryPersistenceAdapter adapter = new BookingHistoryPersistenceAdapter(repository);
        UUID clientId = UUID.randomUUID();
        BookingHistoryProjection projection = mock(BookingHistoryProjection.class);
        when(projection.getService()).thenReturn("Masaje");
        when(projection.getProvider()).thenReturn("Spa");
        when(projection.getFechaHora()).thenReturn(Instant.parse("2026-10-09T15:30:00Z"));
        when(projection.getEstado()).thenReturn("CONFIRMADA");
        when(repository.findBookingHistoryByClientId(clientId)).thenReturn(List.of(projection));

        // Act
        var result = adapter.findByClientIdOrderByDateDesc(clientId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getService()).isEqualTo("Masaje");
        assertThat(result.get(0).getProvider()).isEqualTo("Spa");
        assertThat(result.get(0).getFecha().toString()).isEqualTo("2026-10-09");
        assertThat(result.get(0).getHora().toString()).startsWith("15:30");
    }
}
