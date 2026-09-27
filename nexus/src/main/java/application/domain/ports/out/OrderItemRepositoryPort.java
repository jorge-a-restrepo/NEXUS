package application.domain.ports.out;

import application.domain.Order;
import application.domain.OrderItem;
import application.domain.Product;

import java.util.List;

/**
 * Output port for OrderItem persistence and querying.
 *
 * OrderItem holds the unit price as a fixed snapshot taken at purchase time, so
 * items are never updated once the order is confirmed: the contract exposes
 * creation and querying only.
 *
 * findAllByProduct supports a seller consulting the orders that contain its own
 * products (RG-03).
 */
public interface OrderItemRepositoryPort {

    OrderItem save(OrderItem orderItem);

    List<OrderItem> findAllByOrder(Order order);

    List<OrderItem> findAllByProduct(Product product);
}
