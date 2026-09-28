package application.domain.ports.in;

import application.domain.Buyer;
import application.domain.Cart;
import application.domain.CartItem;
import application.domain.Invoice;
import application.domain.Order;
import application.domain.Product;
import application.domain.ReturnRefund;
import application.domain.User;

import java.util.List;

/**
 * Input port for the BUYER role.
 *
 * Domain 2 restriction: a buyer never manages information belonging to other
 * buyers, nor inventory. Every operation is therefore scoped to the
 * authenticated user received as the first parameter.
 */
public interface BuyerPort {

    Buyer consultMyProfile(User user);

    Buyer updateMyProfile(User user, Buyer buyer);

    // Catalog

    List<Product> consultCatalog(User user);

    Product consultProduct(User user, Product product);

    // Cart (Domain 7: provisional selection stage)

    Cart consultMyCart(User user);

    Cart addItemToCart(User user, CartItem cartItem);

    Cart updateCartItemQuantity(User user, CartItem cartItem);

    Cart removeItemFromCart(User user, CartItem cartItem);

    /**
     * The active cart is resolved from the authenticated buyer, so it is never
     * supplied by the caller.
     */
    Order checkoutCart(User user);

    // Orders

    /**
     * SUPUESTO: the specification states that payment is validated before
     * preparation starts, but does not name the actor who confirms it. The
     * buyer is assumed here, consistent with the responsibility matrix, which
     * assigns order management to the buyer.
     */
    Order confirmOrderPayment(User user, Order order);

    List<Order> consultMyOrders(User user);

    Order consultMyOrder(User user, Order order);

    // Invoicing

    List<Invoice> consultMyInvoices(User user);

    Invoice consultOrderInvoice(User user, Order order);

    // Returns and refunds

    ReturnRefund requestReturn(User user, ReturnRefund returnRefund);

    List<ReturnRefund> consultMyReturns(User user);
}
