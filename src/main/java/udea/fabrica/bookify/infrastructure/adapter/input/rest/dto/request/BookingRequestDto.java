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
    @NotNull 
    private UUID disponibilidadId; 
    private String notas; 
}
