package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "tbl_dia_no_laborable") @Getter @Setter
public class DiaNoLaborableEntity {
    @Id @Column(name = "id_dia_no_laborable") private UUID id;
    @Column(name = "id_proveedor", nullable = false) private UUID providerId;
    @Column(name = "fecha", nullable = false) private LocalDate date;
    @Column(name = "motivo") private String reason;
    @Column(name = "id_usuario_creador", nullable = false) private UUID createdByUserId;
    @Column(name = "fecha_creacion", nullable = false) private OffsetDateTime createdAt;
}
