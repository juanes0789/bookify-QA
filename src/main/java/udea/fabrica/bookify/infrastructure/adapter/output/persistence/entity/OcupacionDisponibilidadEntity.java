package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity 
@Table(name="tbl_ocupacion_disponibilidad") 
@IdClass(OcupacionDisponibilidadEntity.Key.class) 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OcupacionDisponibilidadEntity {
    @Id 
    @Column(name="id_disponibilidad")
    private UUID disponibilidadId; 

    @Id 
    @Column(name="id_reserva")
    private UUID reservaId;

    @Column(name="fecha_ocupacion", nullable=false)
    private OffsetDateTime fechaOcupacion;

    @NoArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    public static class Key implements Serializable {
        private UUID disponibilidadId;
        private UUID reservaId;

        public Key(UUID disponibilidadId, UUID reservaId) {
            this.disponibilidadId = disponibilidadId;
            this.reservaId = reservaId;
        }
    }
}