package udea.fabrica.bookify.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class MerchantProfile {
    private Long id;
    private Long userId;
    private String businessName;
    private String category;
    private String address;
    private String phone;

    public MerchantProfile(Long id, Long userId, String businessName, String category, String address, String phone) {
        this.id = id;
        this.userId = userId;
        this.businessName = businessName;
        this.category = category;
        this.address = address;
        this.phone = phone;
    }
}