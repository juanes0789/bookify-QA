package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import java.time.OffsetDateTime; 
import java.util.UUID; 
import lombok.*;

@Entity 
@Table(name="tbl_historial_estado_reserva") 
@Getter 
@Setter 
public class HistorialEstadoReservaEntity {
    @Id 
    @GeneratedValue(strategy=GenerationType.UUID) 
    private UUID id; 

    @Column(name="reserva_id",nullable=false) 
    private UUID reservaId;

    @Column(name="estado_anterior_id") 
    private Integer estadoAnteriorId; 

    @Column(name="estado_nuevo_id",nullable=false) 
    private Integer estadoNuevoId;

    @Column(name="cambiado_por_usuario_id") 
    private UUID cambiadoPorUsuarioId; 

    @Column(name="motivo") 
    private String motivo; 
    
    @Column(name="fecha_cambio",nullable=false) 
    private OffsetDateTime fechaCambio;
}
