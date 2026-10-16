package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tbl_tipo_recurso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoRecursoEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tipo_recurso") private UUID id;
}
