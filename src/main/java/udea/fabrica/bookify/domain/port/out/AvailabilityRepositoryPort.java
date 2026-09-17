package udea.fabrica.bookify.domain.port.out;
import java.util.*; 
import udea.fabrica.bookify.domain.model.Availability;
public interface AvailabilityRepositoryPort { 
    Optional<Availability> findByIdForUpdate(UUID id); 
    Availability save(Availability a); 
    Integer findAvailabilityStatusIdByCode(String code); 
}
