package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class AvailableScheduleResponseDto {
    private UUID availabilityId;
    private OffsetDateTime inicioAt;
    private OffsetDateTime finAt;
}
