package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.adapters.rest.mappers.BuyerRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.domain.AuthenticationResult;
import application.domain.Buyer;
import application.domain.User;
import application.domain.ports.in.PublicAccessPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication and registration endpoints (/api/v1/auth/** is
 * permitAll in SecurityConfig).
 *
 * Flow: request JSON -> RequestDTO (@Valid) -> mapper -> input port
 * (PublicAccessPort, implemented by PublicAccessUseCaseImpl) -> domain service
 * -> domain model -> mapper -> ResponseDTO.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;
    private final long expirationMs;

    public AuthRestController(PublicAccessPort publicAccessPort,
                              @Value("${jwt.expiration-ms}") long expirationMs) {
        this.publicAccessPort = publicAccessPort;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        AuthenticationResult result = publicAccessPort.login(UserRestMapper.toDomain(requestDTO));
        return ResponseEntity.ok(UserRestMapper.toLoginResponseDTO(result, expirationMs / 1000));
    }

    /** Stateless logout: the client discards the token after this call. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        publicAccessPort.logout(authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@Valid @RequestBody RegisterBuyerRequestDTO requestDTO) {
        Buyer buyer = BuyerRestMapper.toDomain(requestDTO);
        Buyer saved = publicAccessPort.registerBuyer(buyer);
        return ResponseEntity.status(HttpStatus.CREATED).body(BuyerRestMapper.toResponseDTO(saved));
    }
}
