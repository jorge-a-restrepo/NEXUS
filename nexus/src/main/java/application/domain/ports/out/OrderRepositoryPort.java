package application.domain.ports.out;

import application.domain.Buyer;
import application.domain.Order;
import application.domain.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Order persistence and querying.
 *
 * Domain 7 defines the order lifecycle (PENDING_PAYMENT, PAID, SHIPPED,
 * DELIVERED) and the critical validation that a finalized order can never be
 * modified. Enforcing that rule is the responsibility of the domain; this port
 * only provides the persistence contract.
 *
 * SUPUESTO: findByIdentifier requires the Order domain model to expose a unique
 * identifier attribute, which it does not have yet.
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    Order update(Order order);

    Optional<Order> findByIdentifier(Order order);

    List<Order> findAllByBuyer(Buyer buyer);

    List<Order> findAllByStatus(OrderStatus orderStatus);

    List<Order> findAll();
}
