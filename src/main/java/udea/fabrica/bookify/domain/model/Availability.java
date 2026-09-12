package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Availability {
    private UUID id;
    private UUID agendaServicioId;
    private Integer estadoDisponibilidadId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;
    private UUID creadoPorUsuarioId;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    public Availability() {}

    public Availability(UUID id, UUID agendaServicioId, Integer estadoDisponibilidadId,
                        OffsetDateTime inicioAt, OffsetDateTime finAt, UUID creadoPorUsuarioId,
                        OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
        this.id = id;
        this.agendaServicioId = agendaServicioId;
        this.estadoDisponibilidadId = estadoDisponibilidadId;
        this.inicioAt = inicioAt;
        this.finAt = finAt;
        this.creadoPorUsuarioId = creadoPorUsuarioId;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAgendaServicioId() { return agendaServicioId; }
    public void setAgendaServicioId(UUID agendaServicioId) { this.agendaServicioId = agendaServicioId; }

    public Integer getEstadoDisponibilidadId() { return estadoDisponibilidadId; }
    public void setEstadoDisponibilidadId(Integer estadoDisponibilidadId) { this.estadoDisponibilidadId = estadoDisponibilidadId; }

    public OffsetDateTime getInicioAt() { return inicioAt; }
    public void setInicioAt(OffsetDateTime inicioAt) { this.inicioAt = inicioAt; }

    public OffsetDateTime getFinAt() { return finAt; }
    public void setFinAt(OffsetDateTime finAt) { this.finAt = finAt; }

    public UUID getCreadoPorUsuarioId() { return creadoPorUsuarioId; }
    public void setCreadoPorUsuarioId(UUID creadoPorUsuarioId) { this.creadoPorUsuarioId = creadoPorUsuarioId; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}