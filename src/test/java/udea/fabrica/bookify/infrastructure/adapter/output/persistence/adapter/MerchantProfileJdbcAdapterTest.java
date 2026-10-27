package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import udea.fabrica.bookify.domain.model.MerchantProfile;

class MerchantProfileJdbcAdapterTest {

    private JdbcTemplate jdbc;
    private MerchantProfileJdbcAdapter adapter;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        adapter = new MerchantProfileJdbcAdapter(jdbc);
    }

    @Test
    void shouldReturnTrue_whenUserAlreadyHasProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(jdbc.queryForObject(contains("tbl_proveedor_usuario"), eq(Boolean.class), eq(userId)))
                .thenReturn(Boolean.TRUE);

        // Act
        boolean exists = adapter.existsByUserId(userId);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenExistsQueryReturnsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(jdbc.queryForObject(contains("tbl_proveedor_usuario"), eq(Boolean.class), eq(userId)))
                .thenReturn(null);

        // Act
        boolean exists = adapter.existsByUserId(userId);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldMapProfileRow_whenUserHasProfile() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("id_proveedor", UUID.class)).thenReturn(providerId);
        when(rs.getObject("id_usuario", UUID.class)).thenReturn(userId);
        when(rs.getString("nombre_comercial")).thenReturn("Barbería Central");
        when(rs.getString("categoria")).thenReturn("Barbería y belleza");
        when(rs.getString("direccion")).thenReturn("Calle 10 # 40-20");
        when(rs.getString("telefono")).thenReturn("+573001234567");
        when(jdbc.query(contains("FROM tbl_proveedor_usuario"),
                ArgumentMatchers.<RowMapper<MerchantProfile>>any(), eq(userId)))
                .thenAnswer(invocation -> {
                    RowMapper<MerchantProfile> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(rs, 0));
                });

        // Act
        Optional<MerchantProfile> result = adapter.findByUserId(userId);

        // Assert
        assertThat(result).isPresent();
        MerchantProfile profile = result.get();
        assertThat(profile.getId()).isEqualTo(providerId);
        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getBusinessName()).isEqualTo("Barbería Central");
        assertThat(profile.getCategory()).isEqualTo("Barbería y belleza");
        assertThat(profile.getAddress()).isEqualTo("Calle 10 # 40-20");
        assertThat(profile.getPhone()).isEqualTo("+573001234567");
    }

    @Test
    void shouldReturnEmpty_whenUserHasNoProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(jdbc.query(contains("FROM tbl_proveedor_usuario"),
                ArgumentMatchers.<RowMapper<MerchantProfile>>any(), eq(userId)))
                .thenReturn(List.of());

        // Act
        Optional<MerchantProfile> result = adapter.findByUserId(userId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReuseCategoryAndRole_whenBothExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        MerchantProfile profile = profile(userId);
        when(jdbc.queryForList(contains("FROM tbl_categoria_comercial WHERE nombre"), eq(Integer.class),
                eq("Barbería y belleza"))).thenReturn(List.of(3));
        when(jdbc.queryForList(contains("FROM tbl_rol_proveedor ORDER BY"), eq(Integer.class)))
                .thenReturn(List.of(7));

        // Act
        MerchantProfile saved = adapter.save(profile);

        // Assert
        assertThat(saved.getId()).isNotNull();
        verify(jdbc).update(contains("nombre_comercial, id_categoria_comercial"), eq(saved.getId()),
                eq("Barbería Central"), eq(3), eq("Calle 10 # 40-20"), eq("+573001234567"));
        verify(jdbc).update(contains("INSERT INTO tbl_proveedor_usuario"), eq(saved.getId()), eq(userId), eq(7));
        verify(jdbc, never()).update(contains("INSERT INTO tbl_categoria_comercial"), any(), any(), any());
        verify(jdbc, never()).update(contains("INSERT INTO tbl_rol_proveedor"), any(), any());
    }

    @Test
    void shouldCreateCategoryAndRole_whenTheyDoNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        MerchantProfile profile = profile(userId);
        when(jdbc.queryForList(contains("FROM tbl_categoria_comercial WHERE nombre"), eq(Integer.class),
                eq("Barbería y belleza"))).thenReturn(List.of());
        when(jdbc.queryForObject(contains("COALESCE(MAX(id_categoria_comercial)"), eq(Integer.class)))
                .thenReturn(5);
        when(jdbc.queryForList(contains("FROM tbl_rol_proveedor ORDER BY"), eq(Integer.class)))
                .thenReturn(List.of());
        when(jdbc.queryForObject(contains("FROM tbl_rol_proveedor WHERE codigo"), eq(Integer.class),
                eq("PROPIETARIO"))).thenReturn(1);

        // Act
        MerchantProfile saved = adapter.save(profile);

        // Assert
        verify(jdbc).update(contains("INSERT INTO tbl_categoria_comercial"),
                eq(5), eq("BARBERIA_Y_BELLEZA"), eq("Barbería y belleza"));
        verify(jdbc).update(contains("INSERT INTO tbl_rol_proveedor"), eq("PROPIETARIO"), eq("Propietario"));
        verify(jdbc).update(contains("INSERT INTO tbl_proveedor_usuario"), eq(saved.getId()), eq(userId), eq(1));
    }

    private static MerchantProfile profile(UUID userId) {
        return MerchantProfile.builder()
                .userId(userId)
                .businessName("Barbería Central")
                .category("Barbería y belleza")
                .address("Calle 10 # 40-20")
                .phone("+573001234567")
                .build();
    }
}
