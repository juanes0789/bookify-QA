package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;


import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

public class AvailabilityEntityMapper {

    public static AvailabilityEntity toEntity(Availability domain) {
        if (domain == null) return null;
        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setId(domain.getId());
        entity.setAgendaServicioId(domain.getAgendaServicioId());
        entity.setEstadoDisponibilidadId(domain.getEstadoDisponibilidadId());
        entity.setInicioAt(domain.getInicioAt());
        entity.setFinAt(domain.getFinAt());
        entity.setCreadoPorUsuarioId(domain.getCreadoPorUsuarioId());
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaActualizacion(domain.getFechaActualizacion());
        return entity;
    }

    public static Availability toDomain(AvailabilityEntity entity) {
        if (entity == null) return null;
        return new Availability(
                entity.getId(),
                entity.getAgendaServicioId(),
                entity.getEstadoDisponibilidadId(),
                entity.getInicioAt(),
                entity.getFinAt(),
                entity.getCreadoPorUsuarioId(),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion()
        );
    }
}