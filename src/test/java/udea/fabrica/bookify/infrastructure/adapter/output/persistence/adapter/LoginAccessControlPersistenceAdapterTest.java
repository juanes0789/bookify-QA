package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.model.LoginAccessControl;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.ControlAccesoLoginEntity;

@ExtendWith(MockitoExtension.class)
class LoginAccessControlPersistenceAdapterTest {
    @Mock private EntityManager entityManager;
    @Mock private Query query;
    @InjectMocks private LoginAccessControlPersistenceAdapter adapter;

    @Test
    void shouldMapLoginControlToDomain_whenEmailExists() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        ControlAccesoLoginEntity entity = new ControlAccesoLoginEntity("user@bookify.co", 3, now.plusMinutes(15), now, now, now);
        when(entityManager.find(ControlAccesoLoginEntity.class, "user@bookify.co")).thenReturn(entity);

        // Act
        var result = adapter.findByNormalizedEmail("user@bookify.co");

        // Assert
        assertThat(result).hasValueSatisfying(control -> {
            assertThat(control.failedAttempts()).isEqualTo(3);
            assertThat(control.blockedUntil()).isEqualTo(entity.getBlockedUntil());
        });
    }

    @Test
    void shouldPersistNewLoginControl_whenEntityDoesNotExist() {
        // Arrange
        LoginAccessControl control = new LoginAccessControl("user@bookify.co", 5, OffsetDateTime.now().plusMinutes(15), OffsetDateTime.now());
        when(entityManager.find(ControlAccesoLoginEntity.class, control.normalizedEmail())).thenReturn(null);

        // Act
        adapter.save(control);

        // Assert
        ArgumentCaptor<ControlAccesoLoginEntity> captor = ArgumentCaptor.forClass(ControlAccesoLoginEntity.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getNormalizedEmail()).isEqualTo(control.normalizedEmail());
        assertThat(captor.getValue().getFailedAttempts()).isEqualTo(5);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void shouldExecuteDeleteQuery_whenClearingLoginControl() {
        // Arrange
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("email", "user@bookify.co")).thenReturn(query);

        // Act
        adapter.clear("user@bookify.co");

        // Assert
        verify(query).setParameter("email", "user@bookify.co");
        verify(query).executeUpdate();
    }
}
