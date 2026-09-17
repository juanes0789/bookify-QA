package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;
import jakarta.validation.Valid; 
import org.springframework.http.*; 
import org.springframework.web.bind.annotation.*; 
import java.util.UUID;
import udea.fabrica.bookify.domain.model.Booking;
import udea.fabrica.bookify.domain.port.in.*; 
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.BookingRequestDto; 
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.BookingResponseDto;

@RestController 
@RequestMapping("/api/v1/bookings") 
public class BookingController {
    private final CreateBookingUseCase create; 
    private final CancelBookingUseCase cancel;

    public BookingController(CreateBookingUseCase c,CancelBookingUseCase x){
        create=c;
        cancel=x;
    }

    @PostMapping 
    public ResponseEntity<BookingResponseDto> create(@Valid @RequestBody BookingRequestDto r){
        Booking b=new Booking();
        b.setClienteId(r.getClienteId());
        b.setAvailabilityId(r.getDisponibilidadId());
        b.setNotas(r.getNotas());
        return ResponseEntity.status(HttpStatus.CREATED).body(response(create.create(b)));
    }

    @PatchMapping("/{id}/cancel") 
    public BookingResponseDto cancel(@PathVariable UUID id){
        return response(cancel.cancel(id));
    }
    
    private BookingResponseDto response(Booking b){
        return BookingResponseDto.builder()
                .id(b.getId())
                .clienteId(b.getClienteId())
                .disponibilidadId(b.getAvailabilityId())
                .estadoReservaId(b.getEstadoReservaId())
                .fechaReserva(b.getFechaReserva())
                .notas(b.getNotas())
                .build();
    }
}
