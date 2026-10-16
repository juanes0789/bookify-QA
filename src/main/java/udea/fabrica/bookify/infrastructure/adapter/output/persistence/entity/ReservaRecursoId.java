package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @EqualsAndHashCode
public class ReservaRecursoId implements Serializable {
    private UUID reservaId;
    private UUID recursoId;
    public ReservaRecursoId(UUID reservaId, UUID recursoId) { this.reservaId = reservaId; this.recursoId = recursoId; }
}
