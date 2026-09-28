package application.domain.services.order;

import application.domain.Order;
import application.domain.OrderStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Moves an order to SHIPPED once its goods physically leave the warehouse.
 *
 * Domain 7 lifecycle: only a PAID order can be dispatched.
 */
@Service
public class MarkOrderShippedService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public MarkOrderShippedService(OrderRepositoryPort orderRepositoryPort,
                                   AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
    }

    public Order execute(User user, Order order) {
        authorizeLogisticsOperatorOperationService.execute(user);
        Optional<Order> storedOptional = orderRepositoryPort.findByIdentifier(order);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Order");
        }
        Order stored = storedOptional.get();
        if (OrderStatus.DELIVERED.equals(stored.getStatus())) {
            throw new DomainException("A finalized order cannot be modified.");
        }
        if (!OrderStatus.PAID.equals(stored.getStatus())) {
            throw new DomainException("Only a paid order can be dispatched.");
        }
        stored.setStatus(OrderStatus.SHIPPED);
        return orderRepositoryPort.update(stored);
    }
}
