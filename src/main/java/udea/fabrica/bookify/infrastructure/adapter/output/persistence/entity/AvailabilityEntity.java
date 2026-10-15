package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_disponibilidad")
@Getter
@Setter
public class AvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_disponibilidad")
    private UUID id;

    @Column(name = "id_agenda_servicio", nullable = false)
    private UUID agendaServicioId;

    @Column(name = "id_estado_disponibilidad", nullable = false)
    private Integer estadoDisponibilidadId;

    @Column(name = "inicio_at", nullable = false)
    private OffsetDateTime inicioAt;

    @Column(name = "fin_at", nullable = false)
    private OffsetDateTime finAt;

    @Column(name = "creado_por_usuario_id")
    private UUID creadoPorUsuarioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;
}
