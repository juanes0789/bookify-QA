package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tbl_estado_recurso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoRecursoEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_estado_recurso") private UUID id;
}
