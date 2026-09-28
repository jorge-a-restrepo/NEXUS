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
 * Closes an order once delivery has been confirmed.
 *
 * Business flow step "Cierre". DELIVERED is terminal: Section 11 states that a
 * finalized order cannot be modified under any circumstance, so no service may
 * transition an order out of this status.
 */
@Service
public class MarkOrderDeliveredService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public MarkOrderDeliveredService(OrderRepositoryPort orderRepositoryPort,
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
            throw new DomainException("The order has already been finalized.");
        }
        if (!OrderStatus.SHIPPED.equals(stored.getStatus())) {
            throw new DomainException("Only a shipped order can be marked as delivered.");
        }
        stored.setStatus(OrderStatus.DELIVERED);
        return orderRepositoryPort.update(stored);
    }
}
