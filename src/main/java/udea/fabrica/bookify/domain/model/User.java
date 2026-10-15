package udea.fabrica.bookify.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class User {

    private UUID id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String passwordHash;
    private UserRole rol;
    private boolean activo;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    public User(UUID id, String nombres, String apellidos, String correo,
                String passwordHash, UserRole rol, boolean activo,
                OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getNombreCompleto() {
        return (nombres + " " + apellidos).trim();
    }

    public UUID getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
}
