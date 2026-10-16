package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import lombok.*;

@Entity 
@Table(name="tbl_estado_reserva") 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoReservaEntity { 
    @Id 
    @Column(name="id_estado_reserva")
    private Integer id; 
    @Column(name="codigo",nullable=false) 
    private String codigo; 
}
