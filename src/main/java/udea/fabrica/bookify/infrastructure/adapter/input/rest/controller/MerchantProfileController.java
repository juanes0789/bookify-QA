package udea.fabrica.bookify.infrastructure.adapter.input.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import udea.fabrica.bookify.domain.model.MerchantProfile;
import udea.fabrica.bookify.domain.port.in.MerchantProfileUseCase;
import udea.fabrica.bookify.infrastructure.adapter.input.rest.dto.request.CreateMerchantProfileRequest;

@RestController
@RequestMapping("/api/v1/comercio/perfil")
public class MerchantProfileController {

    private final MerchantProfileUseCase merchantProfileUseCase;

    public MerchantProfileController(MerchantProfileUseCase merchantProfileUseCase) {
        this.merchantProfileUseCase = merchantProfileUseCase;
    }

    @PostMapping
    public ResponseEntity<MerchantProfile> createProfile(
            @Valid @RequestBody CreateMerchantProfileRequest request,
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        MerchantProfile profile = merchantProfileUseCase.createProfile(userId, role, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }
}