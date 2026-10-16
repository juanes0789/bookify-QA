package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tbl_tipo_identificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoIdentificacionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tipo_identificacion") private UUID id;
}
