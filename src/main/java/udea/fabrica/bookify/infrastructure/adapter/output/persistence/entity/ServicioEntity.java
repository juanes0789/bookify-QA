package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tbl_servicio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServicioEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_servicio") private UUID id;
    @Column(name = "id_proveedor", nullable = false) private UUID proveedorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor", insertable = false, updatable = false)
    private ProveedorEntity proveedor;
}
