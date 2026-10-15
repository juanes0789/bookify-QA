package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.NonWorkingDayEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NonWorkingDayEntityMapperTest {

    @Test
    void populatesCurrentAndLegacyRequiredColumns() {
        UUID providerId = UUID.randomUUID();
        NonWorkingDay day = new NonWorkingDay(null, providerId, LocalDate.now(), null, null);

        NonWorkingDayEntity entity = NonWorkingDayEntityMapper.toEntity(day);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getLegacyId()).isEqualTo(entity.getId());
        assertThat(entity.getProveedorId()).isEqualTo(providerId);
        assertThat(entity.getLegacyProveedorId()).isEqualTo(providerId);
    }
}
