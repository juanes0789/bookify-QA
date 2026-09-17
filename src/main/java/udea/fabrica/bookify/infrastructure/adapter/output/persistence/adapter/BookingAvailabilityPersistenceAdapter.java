package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;
import org.springframework.stereotype.Component; 
import java.util.*; 
import udea.fabrica.bookify.domain.model.Availability; 
import udea.fabrica.bookify.domain.port.out.AvailabilityRepositoryPort; 
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.*; 
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper.AvailabilityEntityMapper; 
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.*;

@Component 
public class BookingAvailabilityPersistenceAdapter implements AvailabilityRepositoryPort {
    private final AvailabilityJpaRepository a; 
    private final EstadoDisponibilidadJpaRepository s; 

    public BookingAvailabilityPersistenceAdapter(AvailabilityJpaRepository a,EstadoDisponibilidadJpaRepository s){
        this.a=a;this.s=s;
    }

    public Optional<Availability> findByIdForUpdate(UUID id){
        return a.findByIdForUpdate(id).map(AvailabilityEntityMapper::toDomain);
    }

    public Availability save(Availability v){
        return AvailabilityEntityMapper.toDomain(a.save(AvailabilityEntityMapper.toEntity(v)));
    }

    public Integer findAvailabilityStatusIdByCode(String c){
        return s.findByCodigo(c).map(EstadoDisponibilidadEntity::getId).orElse(null);
    }
}
