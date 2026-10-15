package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_estado_disponibilidad")
public class EstadoDisponibilidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_disponibilidad")
    private Integer id;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    public Integer getId() {
        return id;
    }
}
