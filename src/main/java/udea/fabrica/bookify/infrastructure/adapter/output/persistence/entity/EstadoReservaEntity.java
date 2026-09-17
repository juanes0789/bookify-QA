package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;
import jakarta.persistence.*; 
import lombok.Getter;

@Entity 
@Table(name="tbl_estado_reserva") 
@Getter 
public class EstadoReservaEntity { 
    @Id 
    private Integer id; 
    @Column(name="codigo",nullable=false) 
    private String codigo; 
}
