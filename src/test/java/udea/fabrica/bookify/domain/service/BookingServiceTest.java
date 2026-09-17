package udea.fabrica.bookify.domain.service;
import static org.assertj.core.api.Assertions.*; 
import static org.mockito.Mockito.*; 
import java.time.OffsetDateTime; 
import java.util.*; 
import org.junit.jupiter.api.*; 
import udea.fabrica.bookify.domain.exception.*; 
import udea.fabrica.bookify.domain.model.*; 
import udea.fabrica.bookify.domain.port.out.*;

class BookingServiceTest {
    BookingRepositoryPort bookings=mock(BookingRepositoryPort.class); 
    AvailabilityRepositoryPort availability=mock(AvailabilityRepositoryPort.class); 
    BookingService service=new BookingService(bookings,availability);
    
    Availability slot(UUID id,int status,OffsetDateTime start){
        Availability a=new Availability();
        a.setId(id);
        a.setEstadoDisponibilidadId(status);
        a.setInicioAt(start);
        return a;
    }

    @Test void createsBookingAndOccupation(){
        UUID aid=UUID.randomUUID(); 
        Booking b=new Booking();
        b.setClienteId(UUID.randomUUID());
        b.setAvailabilityId(aid); 
        when(availability.findByIdForUpdate(aid)).thenReturn(Optional.of(slot(aid,1,OffsetDateTime.now().plusHours(1))));
        when(availability.findAvailabilityStatusIdByCode("DISPONIBLE")).thenReturn(1);
        when(availability.findAvailabilityStatusIdByCode("OCUPADA")).thenReturn(2);
        when(bookings.findReservationStatusIdByCode("CONFIRMADA")).thenReturn(10);
        when(bookings.save(any())).thenAnswer(i->{Booking x=i.getArgument(0);x.setId(UUID.randomUUID());return x;});
        service.create(b);
        verify(bookings).createOccupation(eq(aid),any());
        verify(availability).save(argThat(x->x.getEstadoDisponibilidadId()==2));
    }

    @Test void rejectsOccupiedSlot(){
        UUID id=UUID.randomUUID();
        Booking b=new Booking();
        b.setClienteId(UUID.randomUUID());
        b.setAvailabilityId(id);
        when(availability.findByIdForUpdate(id)).thenReturn(Optional.of(slot(id,2,OffsetDateTime.now().plusHours(1))));
        when(availability.findAvailabilityStatusIdByCode("DISPONIBLE")).thenReturn(1);
        assertThatThrownBy(()->service.create(b)).isInstanceOf(DoubleBookingException.class);
    }

    @Test void cancelsAndRecordsHistory(){
        UUID id=UUID.randomUUID(),aid=UUID.randomUUID();
        Booking b=new Booking();
        b.setId(id);
        b.setEstadoReservaId(10);
        when(bookings.findById(id)).thenReturn(Optional.of(b));
        when(bookings.findAvailabilityIdByBookingId(id)).thenReturn(Optional.of(aid));
        when(availability.findByIdForUpdate(aid)).thenReturn(Optional.of(slot(aid,2,OffsetDateTime.now().plusHours(1))));
        when(bookings.findReservationStatusIdByCode("CANCELADA")).thenReturn(11);
        when(availability.findAvailabilityStatusIdByCode("DISPONIBLE")).thenReturn(1);
        when(bookings.save(any())).thenAnswer(i->i.getArgument(0));
        service.cancel(id);
        verify(bookings).deleteOccupation(aid,id);
        verify(bookings).saveStatusHistory(any());
    }

    @Test void rejectsPastBookingCancellation(){
        UUID id=UUID.randomUUID(),aid=UUID.randomUUID();
        when(bookings.findById(id)).thenReturn(Optional.of(new Booking()));
        when(bookings.findAvailabilityIdByBookingId(id)).thenReturn(Optional.of(aid));
        when(availability.findByIdForUpdate(aid)).thenReturn(Optional.of(slot(aid,2,OffsetDateTime.now().minusMinutes(1))));
        assertThatThrownBy(()->service.cancel(id)).isInstanceOf(InvalidCancellationException.class);
    }
}
