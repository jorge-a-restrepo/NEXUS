package application.domain.services.seller;

import application.domain.Seller;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Retrieves the profile of the authenticated seller.
 *
 * RG-03: the profile is resolved from the authenticated user, never from an
 * identifier supplied by the caller.
 */
@Service
public class ConsultSellerProfileService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public ConsultSellerProfileService(SellerRepositoryPort sellerRepositoryPort,
                                       AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.sellerRepositoryPort = sellerRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public Seller execute(User user) {
        Seller seller = authorizeSellerOperationService.execute(user);
        Optional<Seller> storedOptional = sellerRepositoryPort.findByIdentifier(seller);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Seller");
        }
        return storedOptional.get();
    }
}
