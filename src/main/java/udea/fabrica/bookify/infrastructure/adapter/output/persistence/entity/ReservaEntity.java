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
    @Column(name="id_reserva")
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

    @Column(name = "disponibilidad_id", nullable = false)
    private UUID disponibilidadId;

    @Column(name = "id_servicio")
    private UUID servicioId;

    @Column(name = "id_proveedor")
    private UUID proveedorId;

    @Column(name = "inicio_at")
    private OffsetDateTime inicioAt;

    @Column(name = "fin_at")
    private OffsetDateTime finAt;

    /** Read-only association used by the resource-availability HQL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_reserva_id", referencedColumnName = "id_estado_reserva", insertable = false, updatable = false)
    private EstadoReservaEntity estado;
}
