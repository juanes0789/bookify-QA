package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request;
import jakarta.validation.constraints.NotNull; 
import java.util.UUID; 
import lombok.Getter; 
import lombok.Setter;

@Getter 
@Setter 
public class BookingRequestDto { 
    @NotNull 
    private UUID clienteId; 
    private UUID disponibilidadId; 
    private UUID servicioId;
    private UUID proveedorId;
    private java.time.OffsetDateTime inicioAt;
    private java.time.OffsetDateTime finAt;
    private String notas; 
}
