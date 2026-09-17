package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import java.io.Serializable; 
import java.time.OffsetDateTime; 
import java.util.UUID; 
import lombok.*;

@Entity 
@Table(name="tbl_ocupacion_disponibilidad") 
@IdClass(OcupacionDisponibilidadEntity.Key.class) 
@Getter 
@Setter 
public class OcupacionDisponibilidadEntity {
    @Id 
    @Column(name="disponibilidad_id") 
    private UUID disponibilidadId; 

    @Id 
    @Column(name="reserva_id") 
    private UUID reservaId;

    @Column(name="fecha_ocupacion",nullable=false) 
    private OffsetDateTime fechaOcupacion;

    @NoArgsConstructor 
    @AllArgsConstructor 
    @EqualsAndHashCode 
    public static class Key implements Serializable { 
        private UUID disponibilidadId; 
        private UUID reservaId; 
    }
}
