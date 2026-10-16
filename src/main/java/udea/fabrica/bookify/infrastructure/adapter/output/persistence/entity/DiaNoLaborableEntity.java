package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "tbl_dia_no_laborable") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiaNoLaborableEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id_dia_no_laborable") private UUID id;
    @Column(name = "id_proveedor", nullable = false) private UUID providerId;
    @Column(name = "fecha", nullable = false) private LocalDate date;
    @Column(name = "motivo") private String reason;
    @Column(name = "id_usuario_creador", nullable = false) private UUID createdByUserId;
    @Column(name = "fecha_creacion", nullable = false) private OffsetDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor", insertable = false, updatable = false)
    private ProveedorEntity proveedor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creador", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    private UsuarioEntity usuarioCreador;
}
