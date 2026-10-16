package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tbl_agenda_servicio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgendaServicioEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_agenda_servicio")
    private UUID id;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
