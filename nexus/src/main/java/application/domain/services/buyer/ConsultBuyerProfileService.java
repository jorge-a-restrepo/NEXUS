package application.domain.services.buyer;

import application.domain.Buyer;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Retrieves the profile of the authenticated buyer.
 *
 * Domain 2: the profile is always resolved from the authenticated user, so a
 * buyer can never read another buyer's information.
 */
@Service
public class ConsultBuyerProfileService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;

    public ConsultBuyerProfileService(BuyerRepositoryPort buyerRepositoryPort,
                                      AuthorizeBuyerOperationService authorizeBuyerOperationService) {
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
    }

    public Buyer execute(User user) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        Optional<Buyer> storedOptional = buyerRepositoryPort.findByIdentifier(buyer);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Buyer");
        }
        return storedOptional.get();
    }
}
