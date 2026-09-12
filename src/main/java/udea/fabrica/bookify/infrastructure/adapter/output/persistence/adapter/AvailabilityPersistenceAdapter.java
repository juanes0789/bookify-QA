package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;


import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.Availability;
import udea.fabrica.bookify.domain.port.out.AvailabilityOutputPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoDisponibilidadEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper.AvailabilityEntityMapper;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AgendaServicioJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.AvailabilityJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.EstadoDisponibilidadJpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class AvailabilityPersistenceAdapter implements AvailabilityOutputPort {

    private final AvailabilityJpaRepository repository;
    private final AgendaServicioJpaRepository agendaServicioRepository;
    private final EstadoDisponibilidadJpaRepository estadoDisponibilidadRepository;

    public AvailabilityPersistenceAdapter(AvailabilityJpaRepository repository,
                                          AgendaServicioJpaRepository agendaServicioRepository,
                                          EstadoDisponibilidadJpaRepository estadoDisponibilidadRepository) {
        this.repository = repository;
        this.agendaServicioRepository = agendaServicioRepository;
        this.estadoDisponibilidadRepository = estadoDisponibilidadRepository;
    }

    @Override
    public Availability save(Availability availability) {
        AvailabilityEntity entity = AvailabilityEntityMapper.toEntity(availability);
        AvailabilityEntity saved = repository.save(entity);
        return AvailabilityEntityMapper.toDomain(saved);
    }

    @Override
    public boolean existsOverlapping(UUID agendaServicioId, OffsetDateTime inicioAt, OffsetDateTime finAt) {
        return repository.existsOverlappingAvailability(agendaServicioId, inicioAt, finAt);
    }

    @Override
    public boolean existsActiveAgendaServicio(UUID agendaServicioId) {
        return agendaServicioRepository.existsByIdAndActivoTrue(agendaServicioId);
    }

    @Override
    public Integer findAvailabilityStatusIdByCode(String codigo) {
        return estadoDisponibilidadRepository.findByCodigo(codigo)
                .map(EstadoDisponibilidadEntity::getId)
                .orElse(null);
    }
}
