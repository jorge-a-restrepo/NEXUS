package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.mappers.BuyerRestMapper;
import application.domain.Buyer;
import application.domain.ports.in.PublicAccessPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication and registration endpoints.
 *
 * Flow: request JSON -> RequestDTO (@Valid) -> mapper -> input port
 * (PublicAccessPort, implemented by PublicAccessUseCaseImpl) -> domain service
 * -> domain model -> mapper -> ResponseDTO.
 *
 * Login and logout are added together with the JWT adapter.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;

    public AuthRestController(PublicAccessPort publicAccessPort) {
        this.publicAccessPort = publicAccessPort;
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@Valid @RequestBody RegisterBuyerRequestDTO requestDTO) {
        Buyer buyer = BuyerRestMapper.toDomain(requestDTO);
        Buyer saved = publicAccessPort.registerBuyer(buyer);
        return ResponseEntity.status(HttpStatus.CREATED).body(BuyerRestMapper.toResponseDTO(saved));
    }
}
