package udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper;

import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UserEntity;

public class UserEntityMapper {

    private UserEntityMapper() {}

    public static UserEntity toEntity(User domain, Integer estadoUsuarioId) {
        if (domain == null) return null;
        UserEntity entity = new UserEntity();
        entity.setEstadoUsuarioId(estadoUsuarioId);
        entity.setNombres(domain.getNombres());
        entity.setApellidos(domain.getApellidos());
        entity.setCorreo(domain.getCorreo());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaActualizacion(domain.getFechaActualizacion());
        return entity;
    }

    public static User toDomain(UserEntity entity, UserRole rol) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getCorreo(),
                null,   // El hash nunca se devuelve en el modelo de respuesta
                rol,
                true,
                entity.getFechaCreacion(),
                entity.getFechaActualizacion()
        );
    }
}
