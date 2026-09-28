package application.domain.services.order;

import application.domain.Order;
import application.domain.OrderItem;
import application.domain.Product;
import application.domain.Seller;
import application.domain.User;
import application.domain.ports.out.OrderItemRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves the orders that contain products of the authenticated seller.
 *
 * The responsibility matrix involves the seller in order management, and RG-03
 * limits that involvement to its own products: the seller reaches an order only
 * because one of its products was purchased in it.
 *
 * Orders are collected through the order lines of the seller's products, and
 * the same order is never returned twice even if it contains several of them.
 */
@Service
public class ConsultSellerOrdersService {

    private final ProductRepositoryPort productRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public ConsultSellerOrdersService(ProductRepositoryPort productRepositoryPort,
                                      OrderItemRepositoryPort orderItemRepositoryPort,
                                      AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.productRepositoryPort = productRepositoryPort;
        this.orderItemRepositoryPort = orderItemRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public List<Order> execute(User user) {
        Seller seller = authorizeSellerOperationService.execute(user);
        List<Product> products = productRepositoryPort.findAllBySeller(seller);
        List<Order> orders = new ArrayList<>();
        if (products == null) {
            return orders;
        }
        for (Product product : products) {
            List<OrderItem> orderItems = orderItemRepositoryPort.findAllByProduct(product);
            if (orderItems == null) {
                continue;
            }
            for (OrderItem orderItem : orderItems) {
                Order order = orderItem.getOrder();
                if (order != null && !containsOrder(orders, order)) {
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    private boolean containsOrder(List<Order> orders, Order candidate) {
        for (Order order : orders) {
            if (order == candidate) {
                return true;
            }
            if (order.getIdentifier() != null && order.getIdentifier().equals(candidate.getIdentifier())) {
                return true;
            }
        }
        return false;
    }
}
