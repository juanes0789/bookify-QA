package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class NonWorkingDay {

    private UUID id;
    private UUID proveedorId;
    private LocalDate fecha;
    private String motivo;
    private OffsetDateTime fechaCreacion;

    public NonWorkingDay() {}

    public NonWorkingDay(UUID id, UUID proveedorId, LocalDate fecha, String motivo, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.proveedorId = proveedorId;
        this.fecha = fecha;
        this.motivo = motivo;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProveedorId() { return proveedorId; }
    public void setProveedorId(UUID proveedorId) { this.proveedorId = proveedorId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
