package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import java.time.OffsetDateTime; 
import java.util.UUID; 
import lombok.Getter; 
import lombok.Setter;

@Entity 
@Table(name="tbl_reserva") 
@Getter 
@Setter 
public class ReservaEntity {
    @Id 
    @GeneratedValue(strategy=GenerationType.UUID) 
    private UUID id;

    @Column(name="cliente_id",nullable=false) 
    private UUID clienteId;

    @Column(name="estado_reserva_id",nullable=false) 
    private Integer estadoReservaId;

    @Column(name="fecha_reserva",nullable=false) 
    private OffsetDateTime fechaReserva;

    @Column(name="notas") 
    private String notas;

    @Column(name="fecha_creacion",nullable=false) 
    private OffsetDateTime fechaCreacion;

    @Column(name="fecha_actualizacion",nullable=false) 
    private OffsetDateTime fechaActualizacion;
}
