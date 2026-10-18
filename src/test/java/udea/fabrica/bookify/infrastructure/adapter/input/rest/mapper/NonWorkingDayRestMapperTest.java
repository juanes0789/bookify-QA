package udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.NonWorkingDayResponseDto;

class NonWorkingDayRestMapperTest {

    @Test
    void shouldCopyEveryField_whenDomainDayIsMappedToResponse() {
        // Arrange
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-01T10:15:00-05:00");
        NonWorkingDay day = new NonWorkingDay(UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2026, 12, 25), "Navidad", createdAt);

        // Act
        NonWorkingDayResponseDto response = NonWorkingDayRestMapper.toResponse(day);

        // Assert
        assertThat(response.getId()).isEqualTo(day.getId());
        assertThat(response.getProveedorId()).isEqualTo(day.getProveedorId());
        assertThat(response.getFecha()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(response.getMotivo()).isEqualTo("Navidad");
        assertThat(response.getFechaCreacion()).isEqualTo(createdAt);
    }
}
