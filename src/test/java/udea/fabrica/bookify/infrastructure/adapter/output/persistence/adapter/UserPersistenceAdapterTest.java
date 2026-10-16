package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import udea.fabrica.bookify.domain.model.User;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.EstadoUsuarioEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.RolEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UserEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.UsuarioRolEntity;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.EstadoUsuarioJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.RolJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.UserJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.UsuarioRolJpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    private UserJpaRepository userJpaRepository;
    private EstadoUsuarioJpaRepository estadoUsuarioJpaRepository;
    private RolJpaRepository rolJpaRepository;
    private UsuarioRolJpaRepository usuarioRolJpaRepository;
    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        userJpaRepository = mock(UserJpaRepository.class);
        estadoUsuarioJpaRepository = mock(EstadoUsuarioJpaRepository.class);
        rolJpaRepository = mock(RolJpaRepository.class);
        usuarioRolJpaRepository = mock(UsuarioRolJpaRepository.class);
        adapter = new UserPersistenceAdapter(
                userJpaRepository,
                estadoUsuarioJpaRepository,
                rolJpaRepository,
                usuarioRolJpaRepository
        );
    }

    @Test
    void shouldSaveUserAndUserRole() {
        UUID savedUserId = UUID.randomUUID();
        givenActiveStatus();
        givenRole("CLIENTE", 7);
        when(userJpaRepository.save(any(UserEntity.class))).thenAnswer(invocation -> savedUser(invocation.getArgument(0), savedUserId));

        adapter.save(newUser(UserRole.CLIENTE));

        verify(estadoUsuarioJpaRepository).findByCodigo("ACTIVO");
        verify(rolJpaRepository).findByCodigo("CLIENTE");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userJpaRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEstadoUsuarioId()).isEqualTo(3);
        assertThat(userCaptor.getValue().getCorreo()).isEqualTo("ana@mail.com");

        ArgumentCaptor<UsuarioRolEntity> userRoleCaptor = ArgumentCaptor.forClass(UsuarioRolEntity.class);
        verify(usuarioRolJpaRepository).save(userRoleCaptor.capture());
        assertThat(userRoleCaptor.getValue().getId().getUsuarioId()).isEqualTo(savedUserId);
        assertThat(userRoleCaptor.getValue().getId().getRolId()).isEqualTo(7);
        assertThat(userRoleCaptor.getValue().getFechaAsignacion()).isNotNull();
    }

    @Test
    void shouldNotReturnPasswordHash() {
        givenActiveStatus();
        givenRole("CLIENTE", 7);
        when(userJpaRepository.save(any(UserEntity.class))).thenAnswer(invocation -> savedUser(invocation.getArgument(0), UUID.randomUUID()));

        User saved = adapter.save(newUser(UserRole.CLIENTE));

        assertThat(saved.getPasswordHash()).isNull();
    }

    @Test
    void shouldThrowWhenActiveStatusDoesNotExist() {
        when(estadoUsuarioJpaRepository.findByCodigo("ACTIVO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.save(newUser(UserRole.CLIENTE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Estado 'ACTIVO' no encontrado en la base de datos");
    }

    @Test
    void shouldThrowWhenRoleDoesNotExist() {
        givenActiveStatus();
        when(rolJpaRepository.findByCodigo("PROVEEDOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.save(newUser(UserRole.PROVEEDOR)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Rol 'PROVEEDOR' no encontrado en la base de datos");
    }

    private void givenActiveStatus() {
        EstadoUsuarioEntity active = new EstadoUsuarioEntity();
        active.setId(3);
        active.setCodigo("ACTIVO");
        active.setNombre("Activo");
        when(estadoUsuarioJpaRepository.findByCodigo("ACTIVO")).thenReturn(Optional.of(active));
    }

    private void givenRole(String code, Integer id) {
        RolEntity role = new RolEntity();
        role.setId(id);
        role.setCodigo(code);
        role.setNombre(code);
        when(rolJpaRepository.findByCodigo(code)).thenReturn(Optional.of(role));
    }

    private User newUser(UserRole role) {
        OffsetDateTime now = OffsetDateTime.now();
        return new User(null, "Ana", "Gomez", "ana@mail.com", "hashed-password", role, true, now, now);
    }

    private UserEntity savedUser(UserEntity entity, UUID id) {
        entity.setId(id);
        return entity;
    }
}
