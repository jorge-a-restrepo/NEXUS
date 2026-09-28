package application.domain.services.cart;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.CartItem;
import application.domain.CartStatus;
import application.domain.Order;
import application.domain.OrderItem;
import application.domain.OrderStatus;
import application.domain.Product;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.CartItemRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.OrderItemRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.inventory.ReserveInventoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Converts the active cart of a buyer into a formal order.
 *
 * Business flow step "Compra": the buyer selects products through the cart and
 * confirms the order. The sequence applied here is:
 *
 * 1. the cart must be ACTIVE and contain at least one item;
 * 2. stock is reserved for every item, which enforces the critical validation
 *    that non-existent or damaged stock cannot be reserved and that available
 *    quantities never become negative;
 * 3. the order is created in PENDING_PAYMENT status (Domain 7);
 * 4. each line is copied into an OrderItem freezing the unit price at purchase
 *    time, so later catalog price changes do not alter closed orders;
 * 5. the cart is marked as CONVERTED and can no longer be modified.
 *
 * SUPUESTO: the reservation resolves the warehouse internally, but OrderItem
 * does not record which warehouse served each line. That association is needed
 * to register the sale outflow and to plan shipments precisely.
 */
@Service
public class CheckoutCartService {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartItemRepositoryPort cartItemRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final ReserveInventoryService reserveInventoryService;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public CheckoutCartService(CartRepositoryPort cartRepositoryPort,
                               CartItemRepositoryPort cartItemRepositoryPort,
                               OrderRepositoryPort orderRepositoryPort,
                               OrderItemRepositoryPort orderItemRepositoryPort,
                               ReserveInventoryService reserveInventoryService,
                               AuthorizeBuyerOperationService authorizeBuyerOperationService,
                               ValidateBuyerOwnershipService validateBuyerOwnershipService) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.cartItemRepositoryPort = cartItemRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderItemRepositoryPort = orderItemRepositoryPort;
        this.reserveInventoryService = reserveInventoryService;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
    }

    public Order execute(User user) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        Optional<Cart> cartOptional = cartRepositoryPort.findActiveByBuyer(buyer);
        if (cartOptional.isEmpty()) {
            throw new EntityNotFoundException("Active cart");
        }
        Cart cart = cartOptional.get();
        validateBuyerOwnershipService.execute(buyer, cart);
        if (!CartStatus.ACTIVE.equals(cart.getStatus())) {
            throw new DomainException("Only an active cart can be checked out.");
        }
        List<CartItem> cartItems = cartItemRepositoryPort.findAllByCart(cart);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new DomainException("The cart does not contain any product.");
        }
        for (CartItem cartItem : cartItems) {
            reserveInventoryService.execute(user, cartItem.getProduct(), cartItem.getQuantity());
        }
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        Order storedOrder = orderRepositoryPort.save(order);
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new EntityNotFoundException("Product of the cart item");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(storedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getBasePrice());
            orderItemRepositoryPort.save(orderItem);
        }
        cart.setStatus(CartStatus.CONVERTED);
        cartRepositoryPort.update(cart);
        return storedOrder;
    }
}
