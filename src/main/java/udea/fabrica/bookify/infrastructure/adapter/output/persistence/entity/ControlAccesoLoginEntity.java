package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.*;

@Entity @Table(name = "tbl_control_acceso_login") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlAccesoLoginEntity {
    @Id @Column(name = "correo_normalizado", nullable = false) private String normalizedEmail;
    @Column(name = "intentos_fallidos", nullable = false) private int failedAttempts;
    @Column(name = "bloqueado_hasta") private OffsetDateTime blockedUntil;
    @Column(name = "ultimo_intento_fallido_at") private OffsetDateTime lastFailedAttemptAt;
    @Column(name = "fecha_creacion", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "fecha_actualizacion", nullable = false) private OffsetDateTime updatedAt;
}
