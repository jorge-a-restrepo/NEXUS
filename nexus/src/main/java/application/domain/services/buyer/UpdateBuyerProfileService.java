package application.domain.services.buyer;

import application.domain.Buyer;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Updates the delivery information of the authenticated buyer.
 *
 * Domain 2 attributes: main address and additional addresses. The commercial
 * status is not modifiable by the buyer: it is a condition granted by the
 * platform, so it is administered through its own service.
 *
 * Identity document, role and status are never modified here, and changing the
 * email re-checks the uniqueness required by Section 11.
 */
@Service
public class UpdateBuyerProfileService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;

    public UpdateBuyerProfileService(BuyerRepositoryPort buyerRepositoryPort,
                                     UserRepositoryPort userRepositoryPort,
                                     AuthorizeBuyerOperationService authorizeBuyerOperationService) {
        this.buyerRepositoryPort = buyerRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
    }

    public Buyer execute(User user, Buyer buyer) {
        Buyer authenticatedBuyer = authorizeBuyerOperationService.execute(user);
        if (buyer == null) {
            throw new DomainException("Buyer information must be provided.");
        }
        Optional<Buyer> storedOptional = buyerRepositoryPort.findByIdentifier(authenticatedBuyer);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Buyer");
        }
        Buyer stored = storedOptional.get();
        if (buyer.getFullName() != null && !buyer.getFullName().isBlank()) {
            stored.setFullName(buyer.getFullName());
        }
        if (buyer.getEmail() != null && !buyer.getEmail().isBlank()
                && !buyer.getEmail().equals(stored.getEmail())) {
            if (userRepositoryPort.existsByEmail(buyer)) {
                throw new DomainException("The email is already registered in the platform.");
            }
            stored.setEmail(buyer.getEmail());
        }
        if (buyer.getMainAddress() != null && !buyer.getMainAddress().isBlank()) {
            stored.setMainAddress(buyer.getMainAddress());
        }
        if (buyer.getAdditionalAddresses() != null) {
            stored.setAdditionalAddresses(buyer.getAdditionalAddresses());
        }
        return buyerRepositoryPort.update(stored);
    }
}
