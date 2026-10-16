package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tbl_proveedor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProveedorEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_proveedor") private UUID id;
    @Column(name = "id_tipo_identificacion") private UUID tipoIdentificacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_identificacion", referencedColumnName = "id_tipo_identificacion", insertable = false, updatable = false)
    private TipoIdentificacionEntity tipoIdentificacion;
}
