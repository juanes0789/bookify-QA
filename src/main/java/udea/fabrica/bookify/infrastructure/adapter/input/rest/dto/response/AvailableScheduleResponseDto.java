package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class AvailableScheduleResponseDto {
    private UUID availabilityId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;

    public AvailableScheduleResponseDto(UUID availabilityId, OffsetDateTime inicioAt, OffsetDateTime finAt) {
        this.availabilityId = availabilityId;
        this.inicioAt = inicioAt;
        this.finAt = finAt;
    }
}