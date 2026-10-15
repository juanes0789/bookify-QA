package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.out.UserRepositoryPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoUsuarioEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.RolEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UserEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UsuarioRolEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper.UserEntityMapper;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.EstadoUsuarioJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.RolJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.UserJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.UsuarioRolJpaRepository;

import java.time.OffsetDateTime;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final EstadoUsuarioJpaRepository estadoUsuarioJpaRepository;
    private final RolJpaRepository rolJpaRepository;
    private final UsuarioRolJpaRepository usuarioRolJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository,
                                  EstadoUsuarioJpaRepository estadoUsuarioJpaRepository,
                                  RolJpaRepository rolJpaRepository,
                                  UsuarioRolJpaRepository usuarioRolJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.estadoUsuarioJpaRepository = estadoUsuarioJpaRepository;
        this.rolJpaRepository = rolJpaRepository;
        this.usuarioRolJpaRepository = usuarioRolJpaRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByCorreoIgnoreCase(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        // Resolver estado ACTIVO
        Integer estadoUsuarioId = estadoUsuarioJpaRepository.findByCodigo("ACTIVO")
                .map(EstadoUsuarioEntity::getId)
                .orElseThrow(() -> new IllegalStateException("Estado 'ACTIVO' no encontrado en la base de datos"));

        // Resolver rol
        Integer rolId = rolJpaRepository.findByCodigo(user.getRol().name())
                .map(RolEntity::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "Rol '" + user.getRol().name() + "' no encontrado en la base de datos"));

        // Guardar usuario en tbl_usuario
        UserEntity entity = UserEntityMapper.toEntity(user, estadoUsuarioId);
        UserEntity saved = userJpaRepository.save(entity);

        // Guardar relación en tbl_usuario_rol
        UsuarioRolEntity usuarioRol = new UsuarioRolEntity();
        usuarioRol.setId(new UsuarioRolEntity.UsuarioRolId(saved.getId(), rolId));
        usuarioRol.setFechaAsignacion(OffsetDateTime.now());
        usuarioRolJpaRepository.save(usuarioRol);

        return UserEntityMapper.toDomain(saved, user.getRol());
    }
}
