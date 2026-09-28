package application.domain.services.cart;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.CartItem;
import application.domain.CartStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.CartItemRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Changes the quantity of a product already present in the active cart.
 *
 * A cart that is no longer ACTIVE has already been converted into an order and
 * cannot be modified.
 */
@Service
public class UpdateCartItemQuantityService {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartItemRepositoryPort cartItemRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public UpdateCartItemQuantityService(CartRepositoryPort cartRepositoryPort,
                                         CartItemRepositoryPort cartItemRepositoryPort,
                                         AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                         ValidateBuyerOwnershipService validateBuyerOwnershipService) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.cartItemRepositoryPort = cartItemRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
    }

    public Cart execute(User user, CartItem cartItem) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        if (cartItem == null || cartItem.getProduct() == null) {
            throw new DomainException("The cart item must be provided.");
        }
        Integer quantity = cartItem.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero.");
        }
        Optional<Cart> cartOptional = cartRepositoryPort.findActiveByBuyer(buyer);
        if (cartOptional.isEmpty()) {
            throw new EntityNotFoundException("Active cart");
        }
        Cart cart = cartOptional.get();
        validateBuyerOwnershipService.execute(buyer, cart);
        if (!CartStatus.ACTIVE.equals(cart.getStatus())) {
            throw new DomainException("Only an active cart can be modified.");
        }
        Optional<CartItem> storedOptional = cartItemRepositoryPort.findByCartAndProduct(cart, cartItem.getProduct());
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Cart item");
        }
        CartItem stored = storedOptional.get();
        stored.setQuantity(quantity);
        cartItemRepositoryPort.update(stored);
        return cart;
    }
}
