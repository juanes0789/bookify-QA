package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.util.UUID;

public class UserResponseDto {

    private UUID id;
    private String nombre;
    private String correo;
    private String rol;

    public UserResponseDto(UUID id, String nombre, String correo, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
}
