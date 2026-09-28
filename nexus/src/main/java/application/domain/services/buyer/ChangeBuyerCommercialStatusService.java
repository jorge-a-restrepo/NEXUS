package application.domain.services.buyer;

import application.domain.Buyer;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Changes the commercial status of a buyer.
 *
 * Domain 2 defines the commercial status as the buyer's condition to perform
 * purchases. It is granted by the platform, so only the ADMINISTRATOR may
 * change it; the buyer never edits it through its profile.
 *
 * SUPUESTO: commercialStatus is still an open String in the domain model, so
 * this service cannot validate the value against a bounded catalog. It must
 * become a value object.
 */
@Service
public class ChangeBuyerCommercialStatusService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public ChangeBuyerCommercialStatusService(BuyerRepositoryPort buyerRepositoryPort,
                                              AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public Buyer execute(User user, Buyer buyer) {
        authorizeAdministratorOperationService.execute(user);
        if (buyer == null) {
            throw new EntityNotFoundException("Buyer");
        }
        if (buyer.getCommercialStatus() == null || buyer.getCommercialStatus().isBlank()) {
            throw new DomainException("The new commercial status must be provided.");
        }
        Optional<Buyer> storedOptional = buyerRepositoryPort.findByIdentifier(buyer);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Buyer");
        }
        Buyer stored = storedOptional.get();
        stored.setCommercialStatus(buyer.getCommercialStatus());
        return buyerRepositoryPort.update(stored);
    }
}
