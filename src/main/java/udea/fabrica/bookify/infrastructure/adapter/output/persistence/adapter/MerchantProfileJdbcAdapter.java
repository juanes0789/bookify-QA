package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.out.MerchantProfileRepositoryPort;

@Repository
public class MerchantProfileJdbcAdapter implements MerchantProfileRepositoryPort {

    private static final String DEFAULT_PROVIDER_ROLE_CODE = "PROPIETARIO";
    private static final String DEFAULT_PROVIDER_ROLE_NAME = "Propietario";

    private final JdbcTemplate jdbc;

    public MerchantProfileJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM tbl_proveedor_usuario WHERE id_usuario = ?)",
                Boolean.class, userId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Optional<MerchantProfile> findByUserId(UUID userId) {
        List<MerchantProfile> result = jdbc.query("""
                SELECT p.id_proveedor, pu.id_usuario, p.nombre_comercial,
                       c.nombre AS categoria, p.direccion, p.telefono
                FROM tbl_proveedor_usuario pu
                JOIN tbl_proveedor p ON p.id_proveedor = pu.id_proveedor
                LEFT JOIN tbl_categoria_comercial c
                       ON c.id_categoria_comercial = p.id_categoria_comercial
                WHERE pu.id_usuario = ?
                LIMIT 1
                """, (rs, rowNum) -> map(rs), userId);
        return result.stream().findFirst();
    }

    @Override
    @Transactional
    public MerchantProfile save(MerchantProfile profile) {
        Integer categoryId = findOrCreateCategory(profile.getCategory());
        Integer providerRoleId = findOrCreateProviderRole();
        UUID providerId = UUID.randomUUID();

        jdbc.update("""
                INSERT INTO tbl_proveedor
                    (id_proveedor, nombre_comercial, id_categoria_comercial, direccion, telefono,
                     activo, fecha_creacion, fecha_actualizacion)
                VALUES (?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                providerId, profile.getBusinessName(), categoryId, profile.getAddress(), profile.getPhone());

        jdbc.update("""
                INSERT INTO tbl_proveedor_usuario
                    (id_proveedor, id_usuario, id_rol_proveedor, activo, fecha_creacion)
                VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP)
                """,
                providerId, profile.getUserId(), providerRoleId);

        profile.setId(providerId);
        return profile;
    }

    private Integer findOrCreateCategory(String name) {
        List<Integer> ids = jdbc.queryForList(
                "SELECT id_categoria_comercial FROM tbl_categoria_comercial WHERE nombre = ?",
                Integer.class, name);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        Integer newId = jdbc.queryForObject(
                "SELECT COALESCE(MAX(id_categoria_comercial), 0) + 1 FROM tbl_categoria_comercial",
                Integer.class);
        jdbc.update(
                "INSERT INTO tbl_categoria_comercial (id_categoria_comercial, codigo, nombre) VALUES (?, ?, ?)",
                newId, toCode(name), name);
        return newId;
    }

    private Integer findOrCreateProviderRole() {
        List<Integer> ids = jdbc.queryForList(
                "SELECT id_rol_proveedor FROM tbl_rol_proveedor ORDER BY id_rol_proveedor LIMIT 1",
                Integer.class);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        jdbc.update("INSERT INTO tbl_rol_proveedor (codigo, nombre) VALUES (?, ?)",
                DEFAULT_PROVIDER_ROLE_CODE, DEFAULT_PROVIDER_ROLE_NAME);
        return jdbc.queryForObject(
                "SELECT id_rol_proveedor FROM tbl_rol_proveedor WHERE codigo = ?",
                Integer.class, DEFAULT_PROVIDER_ROLE_CODE);
    }

    private String toCode(String name) {
        String noAccents = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
    }

    private MerchantProfile map(ResultSet rs) throws SQLException {
        return MerchantProfile.builder()
                .id(rs.getObject("id_proveedor", UUID.class))
                .userId(rs.getObject("id_usuario", UUID.class))
                .businessName(rs.getString("nombre_comercial"))
                .category(rs.getString("categoria"))
                .address(rs.getString("direccion"))
                .phone(rs.getString("telefono"))
                .build();
    }
}
