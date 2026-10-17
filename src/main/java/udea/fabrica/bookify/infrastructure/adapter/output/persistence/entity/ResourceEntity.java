package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_recurso", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comercio_id", "nombre"})
})
@Getter
@Setter
@NoArgsConstructor
@Builder
public class ResourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comercio_id", nullable = false)
    private Long merchantProfileId;

    @Column(name = "nombre", nullable = false, length = 60)
    private String name;

    @Column(name = "tipo", nullable = false)
    private String type;

    @Column(nullable = false)
    private boolean active;

    public ResourceEntity(Long id, Long merchantProfileId, String name, String type, boolean active) {
        this.id = id;
        this.merchantProfileId = merchantProfileId;
        this.name = name;
        this.type = type;
        this.active = active;
    }
}