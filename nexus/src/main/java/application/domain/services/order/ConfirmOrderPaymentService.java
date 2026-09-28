package application.domain.services.order;

import application.domain.Buyer;
import application.domain.Inventory;
import application.domain.Order;
import application.domain.OrderItem;
import application.domain.OrderStatus;
import application.domain.ProductType;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.OrderItemRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeBuyerOperationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.inventory.RegisterSaleOutflowService;
import application.domain.services.invoice.GenerateInvoiceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Confirms the payment of an order and starts the fulfilment flow.
 *
 * Business flow step "Transacción": once payment is validated the order moves
 * from PENDING_PAYMENT to PAID and the invoice is issued (OBJ-09).
 *
 * Domain 5 states that digital products are delivered immediately after
 * payment, so an order containing only digital products is closed as DELIVERED
 * without entering the logistics flow.
 *
 * SUPUESTO: the specification does not name the actor who confirms the payment.
 * The buyer is assumed, consistent with the responsibility matrix. Moving this
 * responsibility to another role only requires changing the authorization used
 * by this service.
 *
 * The definitive sale outflow is registered for every physical line using the
 * warehouse recorded at checkout. Digital lines never consumed inventory, so
 * they produce no movement.
 */
@Service
public class ConfirmOrderPaymentService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final GenerateInvoiceService generateInvoiceService;
    private final RegisterSaleOutflowService registerSaleOutflowService;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public ConfirmOrderPaymentService(OrderRepositoryPort orderRepositoryPort,
                                      OrderItemRepositoryPort orderItemRepositoryPort,
                                      GenerateInvoiceService generateInvoiceService,
                                      RegisterSaleOutflowService registerSaleOutflowService,
                                      AuthorizeBuyerOperationService authorizeBuyerOperationService,
                                      ValidateBuyerOwnershipService validateBuyerOwnershipService) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderItemRepositoryPort = orderItemRepositoryPort;
        this.generateInvoiceService = generateInvoiceService;
        this.registerSaleOutflowService = registerSaleOutflowService;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
        this.validateBuyerOwnershipService = validateBuyerOwnershipService;
    }

    public Order execute(User user, Order order) {
        Buyer buyer = authorizeBuyerOperationService.execute(user);
        Optional<Order> storedOptional = orderRepositoryPort.findByIdentifier(order);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Order");
        }
        Order stored = storedOptional.get();
        validateBuyerOwnershipService.execute(buyer, stored);
        if (!OrderStatus.PENDING_PAYMENT.equals(stored.getStatus())) {
            throw new DomainException("Only an order pending payment can be paid.");
        }
        stored.setStatus(OrderStatus.PAID);
        Order paid = orderRepositoryPort.update(stored);
        generateInvoiceService.execute(paid);
        registerSaleOutflows(user, paid);
        if (containsOnlyDigitalProducts(paid)) {
            paid.setStatus(OrderStatus.DELIVERED);
            paid = orderRepositoryPort.update(paid);
        }
        return paid;
    }

    private void registerSaleOutflows(User user, Order order) {
        List<OrderItem> orderItems = orderItemRepositoryPort.findAllByOrder(order);
        if (orderItems == null) {
            return;
        }
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getWarehouse() == null) {
                continue;
            }
            Inventory reference = new Inventory();
            reference.setProduct(orderItem.getProduct());
            reference.setWarehouse(orderItem.getWarehouse());
            registerSaleOutflowService.execute(user, reference, orderItem.getQuantity());
        }
    }

    private boolean containsOnlyDigitalProducts(Order order) {
        List<OrderItem> orderItems = orderItemRepositoryPort.findAllByOrder(order);
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProduct() == null || !ProductType.DIGITAL.equals(orderItem.getProduct().getProductType())) {
                return false;
            }
        }
        return true;
    }
}
