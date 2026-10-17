package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "tbl_recurso", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comercio_id", "nombre"})
})
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ResourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id_recurso;

    @Column(name = "id_proveedor", nullable = false)
    private UUID merchantProfileId;

    @Column(name = "id_tipo_recurso", nullable = false)
    private UUID type;

    @Column(name = "nombre", nullable = false, length = 60)
    private String name;

    public ResourceEntity(UUID id, UUID merchantProfileId, String name, UUID type) {
        this.id_recurso = id;
        this.merchantProfileId = merchantProfileId;
        this.name = name;
        this.type = type;
    }
}