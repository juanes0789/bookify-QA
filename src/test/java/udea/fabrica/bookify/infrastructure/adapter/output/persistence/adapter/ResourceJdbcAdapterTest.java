package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.model.Resource;

class ResourceJdbcAdapterTest {

    private JdbcTemplate jdbc;
    private ResourceJdbcAdapter adapter;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        adapter = new ResourceJdbcAdapter(jdbc);
    }

    @Test
    void shouldReturnTrue_whenNameExistsInMerchant() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(jdbc.queryForObject(contains("FROM tbl_recurso"), eq(Boolean.class), eq(providerId), eq("Cancha 1")))
                .thenReturn(Boolean.TRUE);

        // Act
        boolean exists = adapter.existsByMerchantProfileIdAndNameIgnoreCase(providerId, "Cancha 1");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenExistsQueryReturnsNull() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(jdbc.queryForObject(contains("FROM tbl_recurso"), eq(Boolean.class), eq(providerId), eq("Cancha 1")))
                .thenReturn(null);

        // Act
        boolean exists = adapter.existsByMerchantProfileIdAndNameIgnoreCase(providerId, "Cancha 1");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldInsertActiveResource_whenTypeAndStatusAreConfigured() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        stubTypeAndStatus(List.of(2), List.of(1));

        // Act
        Resource saved = adapter.save(resource(providerId));

        // Assert
        assertThat(saved.id()).isNotNull();
        assertThat(saved.providerId()).isEqualTo(providerId);
        assertThat(saved.type()).isEqualTo("FISICO");
        assertThat(saved.name()).isEqualTo("Cancha 1");
        assertThat(saved.status()).isEqualTo("ACTIVO");
        verify(jdbc).update(contains("INSERT INTO tbl_recurso"),
                eq(saved.id()), eq(providerId), eq(2), eq(1), eq("Cancha 1"));
    }

    @Test
    void shouldThrowIllegalState_whenTypeIsNotConfigured() {
        // Arrange
        Resource resource = resource(UUID.randomUUID());
        stubTypeAndStatus(List.of(), List.of(1));

        // Act + Assert
        assertThatThrownBy(() -> adapter.save(resource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tipo de recurso no configurado en la base de datos");
        verify(jdbc, never()).update(contains("INSERT INTO tbl_recurso"), any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowIllegalState_whenActiveStatusIsNotConfigured() {
        // Arrange
        Resource resource = resource(UUID.randomUUID());
        stubTypeAndStatus(List.of(2), List.of());

        // Act + Assert
        assertThatThrownBy(() -> adapter.save(resource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Estado ACTIVO no configurado en la base de datos");
    }

    @Test
    void shouldThrowConflict_whenInsertViolatesUniqueName() {
        // Arrange
        Resource resource = resource(UUID.randomUUID());
        stubTypeAndStatus(List.of(2), List.of(1));
        when(jdbc.update(contains("INSERT INTO tbl_recurso"), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // Act + Assert
        assertThatThrownBy(() -> adapter.save(resource))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("Ya existe un recurso con ese nombre en tu comercio");
    }

    private void stubTypeAndStatus(List<Integer> typeIds, List<Integer> statusIds) {
        when(jdbc.queryForList(contains("FROM tbl_tipo_recurso"), eq(Integer.class), eq("FISICO")))
                .thenReturn(typeIds);
        when(jdbc.queryForList(contains("FROM tbl_estado_recurso"), eq(Integer.class), eq("ACTIVO")))
                .thenReturn(statusIds);
    }

    private static Resource resource(UUID providerId) {
        return Resource.builder().providerId(providerId).type("FISICO").name("Cancha 1").build();
    }
}
