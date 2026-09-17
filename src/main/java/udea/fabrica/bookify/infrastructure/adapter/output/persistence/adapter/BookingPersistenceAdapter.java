package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;
import jakarta.persistence.*; 
import org.springframework.stereotype.Component; 
import java.util.*; 
import udea.fabrica.bookify.domain.model.*; 
import udea.fabrica.bookify.domain.port.out.BookingRepositoryPort; 
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.*;

@Component 
public class BookingPersistenceAdapter implements BookingRepositoryPort {
    private final EntityManager em; 

    public BookingPersistenceAdapter(EntityManager e){
        em=e;
    }

    public Booking save(Booking b){ 
        ReservaEntity e=b.getId()==null?new ReservaEntity():em.find(ReservaEntity.class,b.getId()); 
        if(e==null)e=new ReservaEntity(); 
        e.setClienteId(b.getClienteId());
        e.setEstadoReservaId(b.getEstadoReservaId());
        e.setFechaReserva(b.getFechaReserva());e.setNotas(b.getNotas());
        e.setFechaCreacion(b.getFechaCreacion());e.setFechaActualizacion(b.getFechaActualizacion()); 
        if(e.getId()==null)em.persist(e); 
        else em.merge(e); 
        b.setId(e.getId());
        return b; 
    }

    public Optional<Booking> findById(UUID id){ 
        ReservaEntity e=em.find(ReservaEntity.class,id); 
        if(e==null)return Optional.empty(); 
        Booking b=new Booking();
        b.setId(e.getId());
        b.setClienteId(e.getClienteId());
        b.setEstadoReservaId(e.getEstadoReservaId());
        b.setFechaReserva(e.getFechaReserva());
        b.setNotas(e.getNotas());
        b.setFechaCreacion(e.getFechaCreacion());
        b.setFechaActualizacion(e.getFechaActualizacion());
        return Optional.of(b);
    }

    public Optional<UUID> findAvailabilityIdByBookingId(UUID id){ 
        return em.createQuery("select o.disponibilidadId from OcupacionDisponibilidadEntity o where o.reservaId=:id",UUID.class).setParameter("id",id).getResultStream().findFirst(); 
    }

    public void createOccupation(UUID a,UUID b){
        OcupacionDisponibilidadEntity o=new OcupacionDisponibilidadEntity();
        o.setDisponibilidadId(a);o.setReservaId(b);
        o.setFechaOcupacion(java.time.OffsetDateTime.now());
        em.persist(o);
    }

    public void deleteOccupation(UUID a,UUID b){
        em.createQuery("delete from OcupacionDisponibilidadEntity o where o.disponibilidadId=:a and o.reservaId=:b").setParameter("a",a).setParameter("b",b).executeUpdate();
    }
    
    public void saveStatusHistory(BookingStatusHistory h){
        HistorialEstadoReservaEntity e=new HistorialEstadoReservaEntity();
        e.setReservaId(h.reservaId());
        e.setEstadoAnteriorId(h.estadoAnteriorId());
        e.setEstadoNuevoId(h.estadoNuevoId());
        e.setCambiadoPorUsuarioId(h.cambiadoPorUsuarioId());
        e.setMotivo(h.motivo());
        e.setFechaCambio(h.fechaCambio());
        em.persist(e);
    }

    public Integer findReservationStatusIdByCode(String code){
        return em.createQuery("select e.id from EstadoReservaEntity e where upper(e.codigo)=:c",Integer.class)
                .setParameter("c",code.toUpperCase())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
