package udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OccupancyDailyResponseDto(LocalDate fecha, long minutosDisponibles, long minutosBloqueados,
                                        long minutosOcupados, BigDecimal porcentajeOcupacion) { }
