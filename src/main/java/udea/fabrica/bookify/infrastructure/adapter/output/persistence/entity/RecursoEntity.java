package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "tbl_recurso") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecursoEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id_recurso") private UUID id;
    @Column(name = "id_proveedor", nullable = false) private UUID proveedorId;
    @Column(name = "id_tipo_recurso") private UUID tipoRecursoId;
    @Column(name = "id_estado_recurso") private UUID estadoRecursoId;
    @Column(name = "nombre", nullable = false) private String nombre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor", insertable = false, updatable = false)
    private ProveedorEntity proveedor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_recurso", referencedColumnName = "id_tipo_recurso", insertable = false, updatable = false)
    private TipoRecursoEntity tipoRecurso;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_recurso", referencedColumnName = "id_estado_recurso", insertable = false, updatable = false)
    private EstadoRecursoEntity estadoRecurso;
}
