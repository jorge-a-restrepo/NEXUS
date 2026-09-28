package application.domain.services.returnrefund;

import application.domain.Buyer;
import application.domain.Order;
import application.domain.OrderStatus;
import application.domain.ReturnRefund;
import application.domain.ReturnStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ReturnRefundRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Registers a return request submitted by a buyer.
 *
 * OBJ-11 and the responsibility matrix: returns are requested by the buyer.
 * A return only makes sense once the order has been delivered, and a given
 * order can only have one open return request.
 *
 * Requesting a return does not modify the order: Section 11 states that a
 * finalized order cannot be modified under any circumstance.
 */
@Service
public class RequestReturnService {

    private final ReturnRefundRepositoryPort returnRefundRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public RequestReturnService(ReturnRefundRepositoryPort returnRefundRepositoryPort,
                                OrderRepositoryPort orderRepositoryPort,
                                AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                ValidateBuyerOwnershipService validateBuyerOwnershipService) {
        this.returnRefundRepositoryPort = returnRefundRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
    }

    public ReturnRefund execute(User user, ReturnRefund returnRefund) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        if (returnRefund == null || returnRefund.getOrder() == null) {
            throw new DomainException("The return request must reference an order.");
        }
        if (returnRefund.getReason() == null || returnRefund.getReason().isBlank()) {
            throw new DomainException("A reason must be provided for the return request.");
        }
        Optional<Order> orderOptional = orderRepositoryPort.findByIdentifier(returnRefund.getOrder());
        if (orderOptional.isEmpty()) {
            throw new EntityNotFoundException("Order");
        }
        Order order = orderOptional.get();
        validateBuyerOwnershipService.execute(buyer, order);
        if (!OrderStatus.DELIVERED.equals(order.getStatus())) {
            throw new DomainException("Only a delivered order can be returned.");
        }
        if (returnRefundRepositoryPort.findByOrder(order).isPresent()) {
            throw new DomainException("A return request already exists for this order.");
        }
        returnRefund.setOrder(order);
        returnRefund.setStatus(ReturnStatus.REQUESTED);
        returnRefund.setRefundedAmount(null);
        return returnRefundRepositoryPort.save(returnRefund);
    }
}
