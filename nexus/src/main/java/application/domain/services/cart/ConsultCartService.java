package application.domain.services.cart;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Retrieves the active cart of the authenticated buyer.
 *
 * Domain 2: a buyer never accesses information belonging to another buyer, so
 * the cart is always resolved from the authenticated user, never from an
 * identifier supplied by the caller.
 */
@Service
public class ConsultCartService {

    private final CartRepositoryPort cartRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;

    public ConsultCartService(CartRepositoryPort cartRepositoryPort,
                              AuthorizeBuyerOperationService authorizeBuyerOperationService) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
    }

    public Cart execute(User user) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        Optional<Cart> cartOptional = cartRepositoryPort.findActiveByBuyer(buyer);
        if (cartOptional.isEmpty()) {
            throw new EntityNotFoundException("Active cart");
        }
        return cartOptional.get();
    }
}
