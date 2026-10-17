package udea.fabrica.bookify.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import udea.fabrica.bookify.domain.model.LoginAccessControl;
import udea.fabrica.bookify.domain.model.LoginResult;
import udea.fabrica.bookify.domain.model.AuthenticatedUser;
import udea.fabrica.bookify.domain.model.UserRole;
import udea.fabrica.bookify.domain.port.out.LoginAccessControlPort;
import udea.fabrica.bookify.domain.port.out.LoginAuthenticationPort;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock private LoginAccessControlPort accessControls;
    @Mock private LoginAuthenticationPort authentication;
    @InjectMocks private LoginService service;

    @Test
    void shouldBlockLogin_whenFifthFailedAttemptIsGiven() {
        // Arrange
        String email = "  USER@BOOKIFY.CO  ";
        when(accessControls.findByNormalizedEmail("user@bookify.co"))
                .thenReturn(Optional.of(new LoginAccessControl("user@bookify.co", 4, null, OffsetDateTime.now())));
        when(authentication.authenticate("user@bookify.co", "wrong-password")).thenReturn(Optional.empty());

        // Act
        LoginResult result = service.login(email, "wrong-password");

        // Assert
        assertThat(result.authenticated()).isFalse();
        assertThat(result.blocked()).isTrue();
        assertThat(result.blockedUntil()).isAfter(OffsetDateTime.now().plusMinutes(14));
        verify(accessControls).save(argThat(control -> control.failedAttempts() == 5
                && control.blockedUntil() != null
                && control.normalizedEmail().equals("user@bookify.co")));
        verify(accessControls, never()).clear(anyString());
    }

    @Test
    void shouldClearAccessControl_whenCredentialsAreValid() {
        // Arrange
        when(accessControls.findByNormalizedEmail("user@bookify.co")).thenReturn(Optional.empty());
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(), "user@bookify.co", UserRole.CLIENTE);
        when(authentication.authenticate("user@bookify.co", "valid-password"))
                .thenReturn(Optional.of(authenticatedUser));

        // Act
        LoginResult result = service.login("USER@BOOKIFY.CO", "valid-password");

        // Assert
        assertThat(result).isEqualTo(LoginResult.authenticated(authenticatedUser));
        verify(accessControls).clear("user@bookify.co");
        verify(accessControls, never()).save(any());
    }

    @Test
    void shouldReturnBlockedResult_whenCurrentBlockHasNotExpired() {
        // Arrange
        OffsetDateTime blockedUntil = OffsetDateTime.now().plusMinutes(10);
        when(accessControls.findByNormalizedEmail("user@bookify.co"))
                .thenReturn(Optional.of(new LoginAccessControl("user@bookify.co", 5, blockedUntil, OffsetDateTime.now())));

        // Act
        LoginResult result = service.login("user@bookify.co", "any-password");

        // Assert
        assertThat(result).isEqualTo(LoginResult.rejected(true, blockedUntil));
        verifyNoInteractions(authentication);
        verify(accessControls, never()).save(any());
    }
}
