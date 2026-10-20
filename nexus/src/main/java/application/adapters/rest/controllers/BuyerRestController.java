package application.adapters.rest.controllers;

import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.mappers.BuyerRestMapper;
import application.domain.Buyer;
import application.domain.User;
import application.domain.ports.in.BuyerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Buyer endpoints (/api/v1/buyer/** requires role BUYER in SecurityConfig).
 *
 * The request never says who the buyer is: the User rebuilt from the JWT is
 * injected with @AuthenticationPrincipal and passed to the input port, so a
 * buyer can only reach their own information (RG-04).
 */
@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerRestController {

    private final BuyerPort buyerPort;

    public BuyerRestController(BuyerPort buyerPort) {
        this.buyerPort = buyerPort;
    }

    @GetMapping("/profile")
    public ResponseEntity<BuyerResponseDTO> consultMyProfile(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        Buyer buyer = buyerPort.consultMyProfile(authenticatedUser);
        return ResponseEntity.ok(BuyerRestMapper.toResponseDTO(buyer));
    }
}
