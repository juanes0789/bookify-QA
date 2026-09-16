package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.port.in.GetBookingHistoryInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.BookingHistoryListResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.BookingHistoryRestMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas/historial")
public class BookingHistoryController {

    private final GetBookingHistoryInputPort getBookingHistoryInputPort;

    public BookingHistoryController(GetBookingHistoryInputPort getBookingHistoryInputPort) {
        this.getBookingHistoryInputPort = getBookingHistoryInputPort;
    }

    @GetMapping
    public ResponseEntity<BookingHistoryListResponseDto> getBookingHistory(
            @RequestHeader("X-Cliente-Id") UUID clienteId
    ) {
        return ResponseEntity.ok(BookingHistoryRestMapper.toResponse(
                getBookingHistoryInputPort.getHistoryByClient(clienteId)
        ));
    }
}
