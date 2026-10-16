package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_disponibilidad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_disponibilidad")
    private UUID id;

    @Column(name = "id_agenda_servicio", nullable = false)
    private UUID agendaServicioId;

    @Column(name = "id_estado_disponibilidad", nullable = false)
    private Integer estadoDisponibilidadId;

    @Column(name = "id_recurso")
    private UUID recursoId;

    @Column(name = "inicio_at", nullable = false)
    private OffsetDateTime inicioAt;

    @Column(name = "fin_at", nullable = false)
    private OffsetDateTime finAt;

    @Column(name = "id_usuario_creador")
    private UUID creadoPorUsuarioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso", referencedColumnName = "id_recurso", insertable = false, updatable = false)
    private RecursoEntity recurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_disponibilidad", referencedColumnName = "id_estado_disponibilidad", insertable = false, updatable = false)
    private EstadoDisponibilidadEntity estadoDisponibilidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creador", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    private UsuarioEntity usuarioCreador;
}
