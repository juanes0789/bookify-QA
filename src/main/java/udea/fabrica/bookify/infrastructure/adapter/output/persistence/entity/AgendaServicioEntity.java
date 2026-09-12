package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tbl_agenda_servicio")
public class AgendaServicioEntity {

    @Id
    private UUID id;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}