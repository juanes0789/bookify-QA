package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tbl_estado_disponibilidad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoDisponibilidadEntity {

    @Id
    @Column(name = "id_estado_disponibilidad")
    private Integer id;

    @Column(name = "codigo", nullable = false)
    private String codigo;
}
