package udea.fabrica.bookify.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role; // CLIENTE o PROVEEDOR
    private boolean active;

    public User(Long id, String name, String email, String password, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }
}