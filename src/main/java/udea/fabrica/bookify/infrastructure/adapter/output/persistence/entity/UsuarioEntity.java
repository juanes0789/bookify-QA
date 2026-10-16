package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "tbl_usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsuarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario") private UUID id;
}
