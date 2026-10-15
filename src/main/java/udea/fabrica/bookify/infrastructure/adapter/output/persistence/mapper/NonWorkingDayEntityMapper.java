package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.NonWorkingDayEntity;

import java.util.UUID;

public class NonWorkingDayEntityMapper {

    private NonWorkingDayEntityMapper() {}

    public static NonWorkingDayEntity toEntity(NonWorkingDay domain) {
        NonWorkingDayEntity entity = new NonWorkingDayEntity();
        UUID id = domain.getId() == null ? UUID.randomUUID() : domain.getId();
        entity.setId(id);
        entity.setLegacyId(id);
        entity.setProveedorId(domain.getProveedorId());
        entity.setLegacyProveedorId(domain.getProveedorId());
        entity.setFecha(domain.getFecha());
        entity.setMotivo(domain.getMotivo());
        entity.setFechaCreacion(domain.getFechaCreacion());
        return entity;
    }

    public static NonWorkingDay toDomain(NonWorkingDayEntity entity) {
        return new NonWorkingDay(
                entity.getId(),
                entity.getProveedorId(),
                entity.getFecha(),
                entity.getMotivo(),
                entity.getFechaCreacion()
        );
    }
}
