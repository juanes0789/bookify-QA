package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import org.junit.jupiter.api.Test;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.DiaNoLaborableEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NonWorkingDayEntityMapperTest {

    @Test
    void diaNoLaborableEntity_populatesAllRequiredColumns() {
        UUID id = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        OffsetDateTime createdAt = OffsetDateTime.now();

        DiaNoLaborableEntity entity = new DiaNoLaborableEntity();
        entity.setId(id);
        entity.setProviderId(providerId);
        entity.setDate(date);
        entity.setReason("Mantenimiento");
        entity.setCreatedByUserId(userId);
        entity.setCreatedAt(createdAt);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getProviderId()).isEqualTo(providerId);
        assertThat(entity.getDate()).isEqualTo(date);
        assertThat(entity.getReason()).isEqualTo("Mantenimiento");
        assertThat(entity.getCreatedByUserId()).isEqualTo(userId);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void nonWorkingDayDomain_roundtripsViaEntity() {
        UUID id = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(2);
        OffsetDateTime createdAt = OffsetDateTime.now();

        NonWorkingDay domain = new NonWorkingDay(id, providerId, date, "Festivo", userId, createdAt);

        DiaNoLaborableEntity entity = new DiaNoLaborableEntity();
        entity.setId(domain.getId());
        entity.setProviderId(domain.getProveedorId());
        entity.setDate(domain.getFecha());
        entity.setReason(domain.getMotivo());
        entity.setCreatedByUserId(domain.getCreatedByUserId());
        entity.setCreatedAt(domain.getFechaCreacion());

        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getProviderId()).isEqualTo(domain.getProveedorId());
        assertThat(entity.getDate()).isEqualTo(domain.getFecha());
        assertThat(entity.getReason()).isEqualTo(domain.getMotivo());
        assertThat(entity.getCreatedByUserId()).isEqualTo(domain.getCreatedByUserId());
        assertThat(entity.getCreatedAt()).isEqualTo(domain.getFechaCreacion());
    }
}
