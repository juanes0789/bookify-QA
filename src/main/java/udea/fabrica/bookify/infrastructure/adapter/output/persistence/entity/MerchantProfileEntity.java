package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_perfil_comercial")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class MerchantProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "nombre_comercial", nullable = false, length = 100)
    private String businessName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    public MerchantProfileEntity(UUID id, UUID userId, String businessName, String category, String address, String phone) {
        this.id = id;
        this.userId = userId;
        this.businessName = businessName;
        this.category = category;
        this.address = address;
        this.phone = phone;
    }
}