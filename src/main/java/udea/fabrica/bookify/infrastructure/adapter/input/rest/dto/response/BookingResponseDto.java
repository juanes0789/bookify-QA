package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;
import java.time.OffsetDateTime; 
import java.util.UUID; 
import lombok.*;

@Getter 
@Setter 
@Builder 
public class BookingResponseDto { 
    private UUID id, clienteId, disponibilidadId; 
    private Integer estadoReservaId; 
    private OffsetDateTime fechaReserva; 
    private String notas; 
}
