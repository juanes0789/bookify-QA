package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import java.time.OffsetDateTime; 
import java.util.UUID; 
import lombok.*;

@Entity 
@Table(name="tbl_historial_estado_reserva") 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialEstadoReservaEntity {
    @Id 
    @GeneratedValue(strategy=GenerationType.UUID) 
    @Column(name="id_historial_estado_reserva")
    private UUID id; 

    @Column(name="id_reserva",nullable=false)
    private UUID reservaId;

    @Column(name="id_estado_anterior")
    private Integer estadoAnteriorId; 

    @Column(name="id_estado_nuevo",nullable=false)
    private Integer estadoNuevoId;

    @Column(name="id_usuario_cambio")
    private UUID cambiadoPorUsuarioId; 

    @Column(name="motivo") 
    private String motivo; 
    
    @Column(name="fecha_cambio",nullable=false) 
    private OffsetDateTime fechaCambio;
}
