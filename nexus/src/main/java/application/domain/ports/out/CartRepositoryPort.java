package application.domain.ports.out;

import application.domain.Buyer;
import application.domain.Cart;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Cart persistence and querying.
 *
 * Domain 7: the cart is the provisional selection stage that precedes an Order.
 * A buyer operates over a single ACTIVE cart at a time, which is why the
 * primary lookup is by buyer and active status.
 */
public interface CartRepositoryPort {

    Cart save(Cart cart);

    Cart update(Cart cart);

    Optional<Cart> findActiveByBuyer(Buyer buyer);

    List<Cart> findAllByBuyer(Buyer buyer);
}
