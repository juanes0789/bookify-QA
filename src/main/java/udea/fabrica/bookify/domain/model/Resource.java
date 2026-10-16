package udea.fabrica.bookify.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class Resource {
    private Long id;
    private Long merchantProfileId;
    private String name;
    private String type; // FISICO o HUMANO
    private boolean active;

    public Resource(Long id, Long merchantProfileId, String name, String type, boolean active) {
        this.id = id;
        this.merchantProfileId = merchantProfileId;
        this.name = name;
        this.type = type;
        this.active = active;
    }
}