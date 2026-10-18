package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import udea.fabrica.bookify.domain.model.AuthenticatedUser;
import udea.fabrica.bookify.domain.model.UserRole;

@ExtendWith(MockitoExtension.class)
class JpaLoginAuthenticationAdapterTest {
    private static final String EMAIL = "carlos@barberia.com";
    private static final String PASSWORD = "MiContrasenaSegura2026!";
    private static final String STORED_HASH = "$2a$12$hashAlmacenadoDePrueba";

    @Mock private EntityManager entityManager;
    @Mock private Query query;
    @Mock private PasswordEncoder passwordEncoder;
    private JpaLoginAuthenticationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaLoginAuthenticationAdapter(passwordEncoder);
        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);
    }

    @Test
    void shouldReturnAuthenticatedUser_whenPasswordMatchesStoredHash() {
        // Arrange
        UUID userId = UUID.randomUUID();
        stubQueryReturning(Collections.singletonList(new Object[] {userId, EMAIL, STORED_HASH, "PROVEEDOR"}));
        when(passwordEncoder.matches(PASSWORD, STORED_HASH)).thenReturn(true);

        // Act
        Optional<AuthenticatedUser> result = adapter.authenticate(EMAIL, PASSWORD);

        // Assert
        assertThat(result).contains(new AuthenticatedUser(userId, EMAIL, UserRole.PROVEEDOR));
        verify(query).setParameter("correo", EMAIL);
        verify(query).setMaxResults(1);
    }

    @Test
    void shouldReturnEmpty_whenPasswordDoesNotMatch() {
        // Arrange
        stubQueryReturning(Collections.singletonList(new Object[] {UUID.randomUUID(), EMAIL, STORED_HASH, "CLIENTE"}));
        when(passwordEncoder.matches("ContrasenaIncorrecta2026", STORED_HASH)).thenReturn(false);

        // Act
        Optional<AuthenticatedUser> result = adapter.authenticate(EMAIL, "ContrasenaIncorrecta2026");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWithoutCheckingPassword_whenEmailIsNotRegistered() {
        // Arrange
        stubQueryReturning(List.of());

        // Act
        Optional<AuthenticatedUser> result = adapter.authenticate("nadie@mail.com", PASSWORD);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldReturnEmptyWithoutQuerying_whenPasswordIsNull() {
        // Arrange
        String nullPassword = null;

        // Act
        Optional<AuthenticatedUser> result = adapter.authenticate(EMAIL, nullPassword);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(entityManager, passwordEncoder);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubQueryReturning(List rows) {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("correo"), any())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(rows);
    }
}
