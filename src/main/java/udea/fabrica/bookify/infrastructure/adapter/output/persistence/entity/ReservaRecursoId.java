package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ReservaRecursoId implements Serializable {
    private UUID reservaId;
    private UUID recursoId;
    public ReservaRecursoId(UUID reservaId, UUID recursoId) { this.reservaId = reservaId; this.recursoId = recursoId; }
}
