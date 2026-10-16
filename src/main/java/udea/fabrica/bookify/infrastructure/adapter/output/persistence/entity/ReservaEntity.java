package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import java.time.OffsetDateTime; 
import java.util.UUID; 
import lombok.*;

@Entity 
@Table(name="tbl_reserva") 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservaEntity {
    @Id 
    @GeneratedValue(strategy=GenerationType.UUID)
    @Column(name="id_reserva")
    private UUID id;

    @Column(name="id_cliente",nullable=false)
    private UUID clienteId;

    @Column(name="id_estado_reserva",nullable=false)
    private Integer estadoReservaId;

    @Column(name="fecha_reserva",nullable=false) 
    private OffsetDateTime fechaReserva;

    @Column(name="notas") 
    private String notas;

    @Column(name="fecha_creacion",nullable=false) 
    private OffsetDateTime fechaCreacion;

    @Column(name="fecha_actualizacion",nullable=false) 
    private OffsetDateTime fechaActualizacion;

    @Column(name = "id_disponibilidad", nullable = false)
    private UUID disponibilidadId;

    @Column(name = "id_servicio")
    private UUID servicioId;

    @Transient private UUID proveedorId;
    @Transient private OffsetDateTime inicioAt;
    @Transient private OffsetDateTime finAt;

    /** Read-only association used by the resource-availability HQL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_reserva", referencedColumnName = "id_estado_reserva", insertable = false, updatable = false)
    private EstadoReservaEntity estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_disponibilidad", referencedColumnName = "id_disponibilidad", insertable = false, updatable = false)
    private AvailabilityEntity disponibilidad;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    private UsuarioEntity cliente;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", referencedColumnName = "id_servicio", insertable = false, updatable = false)
    private ServicioEntity servicio;
}
