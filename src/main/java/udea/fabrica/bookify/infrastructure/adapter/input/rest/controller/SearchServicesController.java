package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.port.in.SearchServicesInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.SearchServicesResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.SearchServicesRestMapper;

@RestController
@RequestMapping("/api/v1/catalog/services")
public class SearchServicesController {

    private final SearchServicesInputPort searchServicesInputPort;

    public SearchServicesController(SearchServicesInputPort searchServicesInputPort) {
        this.searchServicesInputPort = searchServicesInputPort;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchServicesResponseDto> searchServices(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(SearchServicesRestMapper.toResponse(
                searchServicesInputPort.search(texto, categoria)
        ));
    }
}
