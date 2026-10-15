package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.NonWorkingDay;
import udea.fabrica.bookify.domain.port.out.NonWorkingDayOutputPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.NonWorkingDayEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper.NonWorkingDayEntityMapper;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.NonWorkingDayJpaRepository;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class NonWorkingDayPersistenceAdapter implements NonWorkingDayOutputPort {

    private final NonWorkingDayJpaRepository repository;

    public NonWorkingDayPersistenceAdapter(NonWorkingDayJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public NonWorkingDay save(NonWorkingDay nonWorkingDay) {
        NonWorkingDayEntity entity = NonWorkingDayEntityMapper.toEntity(nonWorkingDay);
        NonWorkingDayEntity saved = repository.save(entity);
        return NonWorkingDayEntityMapper.toDomain(saved);
    }

    @Override
    public boolean existsByProveedorIdAndFecha(UUID proveedorId, LocalDate fecha) {
        return repository.existsByProveedorIdAndFecha(proveedorId, fecha);
    }

    @Override
    public boolean isNonWorkingDay(UUID proveedorId, LocalDate fecha) {
        return repository.existsByProveedorIdAndFecha(proveedorId, fecha);
    }

    @Override
    public UUID findProviderIdByAgendaServicioId(UUID agendaServicioId) {
        return repository.findProviderIdByAgendaServicioId(agendaServicioId);
    }
}
