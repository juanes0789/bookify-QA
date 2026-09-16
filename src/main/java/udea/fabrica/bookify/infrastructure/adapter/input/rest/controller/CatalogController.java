package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udea.fabrica.bookify.domain.port.in.GetServiceCatalogInputPort;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.response.ServiceOfferResponseDto;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.mapper.ServiceCatalogRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/services")
public class CatalogController {

    private final GetServiceCatalogInputPort getServiceCatalogInputPort;

    public CatalogController(GetServiceCatalogInputPort getServiceCatalogInputPort) {
        this.getServiceCatalogInputPort = getServiceCatalogInputPort;
    }

    @GetMapping
    public ResponseEntity<List<ServiceOfferResponseDto>> getAvailableServices() {
        return ResponseEntity.ok(ServiceCatalogRestMapper.toResponse(
                getServiceCatalogInputPort.getAvailableServices()
        ));
    }
}
