package application.domain.services.cart;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.CartItem;
import application.domain.CartStatus;
import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.CartItemRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Adds a product to the active cart of the authenticated buyer.
 *
 * Domain 5: only PUBLISHED products can be purchased. If the buyer has no
 * active cart, one is created, since the cart is the provisional selection
 * stage that precedes the order (Domain 7).
 *
 * Adding a product already present increases its quantity instead of creating a
 * duplicate line.
 */
@Service
public class AddCartItemService {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartItemRepositoryPort cartItemRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;

    public AddCartItemService(CartRepositoryPort cartRepositoryPort,
                              CartItemRepositoryPort cartItemRepositoryPort,
                              ProductRepositoryPort productRepositoryPort,
                              AuthorizeBuyerOperationService authorizeBuyerOperationService) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.cartItemRepositoryPort = cartItemRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
    }

    public Cart execute(User user, CartItem cartItem) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        if (cartItem == null || cartItem.getProduct() == null) {
            throw new DomainException("The product to add must be provided.");
        }
        Integer quantity = cartItem.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero.");
        }
        Optional<Product> productOptional = productRepositoryPort.findByIdentifier(cartItem.getProduct());
        if (productOptional.isEmpty()) {
            throw new EntityNotFoundException("Product");
        }
        Product product = productOptional.get();
        if (!ProductStatus.PUBLISHED.equals(product.getStatus())) {
            throw new DomainException("Only published products can be added to the cart.");
        }
        Cart cart = resolveActiveCart(buyer);
        Optional<CartItem> existingOptional = cartItemRepositoryPort.findByCartAndProduct(cart, product);
        if (existingOptional.isPresent()) {
            CartItem existing = existingOptional.get();
            int currentQuantity = existing.getQuantity() == null ? 0 : existing.getQuantity();
            existing.setQuantity(currentQuantity + quantity);
            cartItemRepositoryPort.update(existing);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepositoryPort.save(newItem);
        }
        return cart;
    }

    private Cart resolveActiveCart(Buyer buyer) {
        Optional<Cart> cartOptional = cartRepositoryPort.findActiveByBuyer(buyer);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        }
        Cart cart = new Cart();
        cart.setBuyer(buyer);
        cart.setStatus(CartStatus.ACTIVE);
        return cartRepositoryPort.save(cart);
    }
}
