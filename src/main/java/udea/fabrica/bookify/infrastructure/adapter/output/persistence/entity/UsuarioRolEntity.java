package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_usuario_rol")
@Getter
@Setter
public class UsuarioRolEntity {

    @EmbeddedId
    private UsuarioRolId id;

    @Column(name = "fecha_asignacion", nullable = false)
    private OffsetDateTime fechaAsignacion;

    @Embeddable
    @Getter
    @Setter
    public static class UsuarioRolId implements Serializable {

        @Column(name = "id_usuario", nullable = false)
        private UUID usuarioId;

        @Column(name = "id_rol", nullable = false)
        private Integer rolId;

        public UsuarioRolId() {}

        public UsuarioRolId(UUID usuarioId, Integer rolId) {
            this.usuarioId = usuarioId;
            this.rolId = rolId;
        }
    }
}
