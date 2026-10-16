package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "tbl_recurso") @Getter @Setter
public class RecursoEntity {
    @Id @Column(name = "id_recurso") private UUID id;
    @Column(name = "id_proveedor", nullable = false) private UUID proveedorId;
    @Column(name = "id_tipo_recurso") private UUID tipoRecursoId;
    @Column(name = "nombre", nullable = false) private String nombre;
}
