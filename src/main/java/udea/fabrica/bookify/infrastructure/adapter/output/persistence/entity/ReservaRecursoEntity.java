package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "tbl_reserva_recurso") @IdClass(ReservaRecursoId.class) @Getter @Setter
public class ReservaRecursoEntity {
    @Id @Column(name = "id_reserva") private java.util.UUID reservaId;
    @Id @Column(name = "id_recurso") private java.util.UUID recursoId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", referencedColumnName = "id_reserva", insertable = false, updatable = false) private ReservaEntity reserva;
}
