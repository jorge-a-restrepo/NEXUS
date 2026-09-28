package application.domain.services.order;

import application.domain.Buyer;
import application.domain.Order;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Retrieves the orders of the authenticated buyer.
 *
 * Domain 2: a buyer never sees orders belonging to another buyer, which is
 * verified through the ownership validation even when the order identifier is
 * supplied by the caller.
 */
@Service
public class ConsultOrderService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public ConsultOrderService(OrderRepositoryPort orderRepositoryPort,
                               AuthorizeBuyerOperationService authorizeBuyerOperationService,
                               ValidateBuyerOwnershipService validateBuyerOwnershipService) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
    }

    public List<Order> executeMyOrders(User user) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        return orderRepositoryPort.findAllByBuyer(buyer);
    }

    public Order execute(User user, Order order) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        Optional<Order> storedOptional = orderRepositoryPort.findByIdentifier(order);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Order");
        }
        Order stored = storedOptional.get();
        validateBuyerOwnershipService.execute(buyer, stored);
        return stored;
    }
}
