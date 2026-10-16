package udea.fabrica.bookify.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NonWorkingDay(UUID id, UUID providerId, LocalDate date, String reason,
                            UUID createdByUserId, OffsetDateTime createdAt) { }
