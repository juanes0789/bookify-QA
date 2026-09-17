package udea.fabrica.bookify.domain.service;
import java.time.OffsetDateTime; 
import java.util.*; 
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.exception.*; 
import udea.fabrica.bookify.domain.model.*; 
import udea.fabrica.bookify.domain.port.in.*; 
import udea.fabrica.bookify.domain.port.out.*;

public class BookingService implements CreateBookingUseCase, CancelBookingUseCase {
    private final BookingRepositoryPort bookings; 
    private final AvailabilityRepositoryPort availability;

    public BookingService(BookingRepositoryPort b, AvailabilityRepositoryPort a){
        bookings=b;availability=a;
    }

    @Transactional 
    public Booking create(Booking b){
        if(b==null||b.getClienteId()==null||b.getAvailabilityId()==null) throw new IllegalArgumentException("El cliente y la disponibilidad son requeridos.");
        Availability a=availability.findByIdForUpdate(b.getAvailabilityId()).orElseThrow(()->new DoubleBookingException("La disponibilidad no existe o ya no puede reservarse."));
        if(!status("DISPONIBLE").equals(a.getEstadoDisponibilidadId())) throw new DoubleBookingException("La disponibilidad ya fue reservada.");
        OffsetDateTime now=OffsetDateTime.now(); b.setEstadoReservaId(reservationStatus("CONFIRMADA")); b.setFechaReserva(now); b.setFechaCreacion(now); b.setFechaActualizacion(now);
        Booking saved=bookings.save(b); 
        bookings.createOccupation(a.getId(),saved.getId()); 
        a.setEstadoDisponibilidadId(status("OCUPADA")); 
        a.setFechaActualizacion(now); 
        availability.save(a); 
        saved.setAvailabilityId(a.getId()); 
        return saved;
    }

    @Transactional 
    public Booking cancel(UUID id){
        Booking b=bookings.findById(id).orElseThrow(()->new BookingNotFoundException("No existe la reserva solicitada.")); UUID aid=bookings.findAvailabilityIdByBookingId(id).orElseThrow(()->new BookingNotFoundException("La reserva no tiene una disponibilidad asociada."));
        Availability a=availability.findByIdForUpdate(aid).orElseThrow(()->new BookingNotFoundException("La disponibilidad asociada no existe."));
        if(!a.getInicioAt().isAfter(OffsetDateTime.now())) throw new InvalidCancellationException("No se puede cancelar una reserva cuyo horario ya inici?.");
        Integer cancelled=reservationStatus("CANCELADA"); 
        if(cancelled.equals(b.getEstadoReservaId())) throw new InvalidCancellationException("La reserva ya est? cancelada.");
        Integer previous=b.getEstadoReservaId(); 
        OffsetDateTime now=OffsetDateTime.now(); 
        b.setEstadoReservaId(cancelled); 
        b.setFechaActualizacion(now); 
        Booking saved=bookings.save(b);
        bookings.deleteOccupation(aid,id); 
        a.setEstadoDisponibilidadId(status("DISPONIBLE")); 
        a.setFechaActualizacion(now); 
        availability.save(a); 
        bookings.saveStatusHistory(new BookingStatusHistory(id,previous,cancelled,null,"Cancelaci?n solicitada por el cliente",now)); 
        saved.setAvailabilityId(aid); 
        return saved;
    }

    private Integer status(String code){
        Integer i=availability.findAvailabilityStatusIdByCode(code);
        if(i==null)throw new IllegalStateException("No existe el estado de disponibilidad '"+code+"'.");
        return i;
    }
    private Integer reservationStatus(String code){
        Integer i=bookings.findReservationStatusIdByCode(code);
        if(i==null)throw new IllegalStateException("No existe el estado de reserva '"+code+"'.");
        return i;
    }
}
