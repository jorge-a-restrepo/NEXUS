package application.domain.services.shipment;

import application.domain.LogisticsOperator;
import application.domain.Order;
import application.domain.OrderItem;
import application.domain.OrderStatus;
import application.domain.ProductType;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.OrderItemRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Prepares a shipment for a paid order.
 *
 * OBJ-10: shipments exist only for physical products; Domain 5 states digital
 * products are delivered immediately after payment, so an order without
 * physical items cannot generate a shipment.
 *
 * Because inventory is distributed (Domain 6), an order may require more than
 * one shipment, each bound to the warehouse that dispatches it.
 */
@Service
public class CreateShipmentService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public CreateShipmentService(ShipmentRepositoryPort shipmentRepositoryPort,
                                 OrderRepositoryPort orderRepositoryPort,
                                 OrderItemRepositoryPort orderItemRepositoryPort,
                                 WarehouseRepositoryPort warehouseRepositoryPort,
                                 AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService) {
        this.shipmentRepositoryPort = shipmentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderItemRepositoryPort = orderItemRepositoryPort;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
    }

    public Shipment execute(User user, Shipment shipment) {
        LogisticsOperator logisticsOperator = authorizeLogisticsOperatorOperationService.execute(user);
        if (shipment == null || shipment.getOrder() == null) {
            throw new DomainException("The shipment must reference an order.");
        }
        if (shipment.getWarehouse() == null) {
            throw new DomainException("The shipment must reference the dispatching warehouse.");
        }
        Optional<Order> orderOptional = orderRepositoryPort.findByIdentifier(shipment.getOrder());
        if (orderOptional.isEmpty()) {
            throw new EntityNotFoundException("Order");
        }
        Order order = orderOptional.get();
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw new DomainException("Only a paid order can be prepared for shipment.");
        }
        if (warehouseRepositoryPort.findByIdentifier(shipment.getWarehouse()).isEmpty()) {
            throw new EntityNotFoundException("Warehouse");
        }
        if (!containsPhysicalProducts(order)) {
            throw new DomainException("An order without physical products does not generate shipments.");
        }
        shipment.setOrder(order);
        shipment.setLogisticsOperator(logisticsOperator);
        shipment.setStatus(ShipmentStatus.IN_PROGRESS);
        return shipmentRepositoryPort.save(shipment);
    }

    private boolean containsPhysicalProducts(Order order) {
        List<OrderItem> orderItems = orderItemRepositoryPort.findAllByOrder(order);
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProduct() != null && ProductType.PHYSICAL.equals(orderItem.getProduct().getProductType())) {
                return true;
            }
        }
        return false;
    }
}
