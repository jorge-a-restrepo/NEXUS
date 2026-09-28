package application.domain.services.shipment;

import application.domain.Order;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import application.domain.services.order.MarkOrderDeliveredService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Confirms the delivery of a shipment and closes the order when no other
 * shipment of the same order remains in progress.
 *
 * Business flow step "Cierre": the order is finalized only after delivery is
 * confirmed. Since a single order may require several shipments, the order is
 * closed only when every shipment has been completed.
 */
@Service
public class CompleteShipmentService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final MarkOrderDeliveredService markOrderDeliveredService;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public CompleteShipmentService(ShipmentRepositoryPort shipmentRepositoryPort,
                                   MarkOrderDeliveredService markOrderDeliveredService,
                                   AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService) {
        this.shipmentRepositoryPort = shipmentRepositoryPort;
        this.markOrderDeliveredService = markOrderDeliveredService;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
    }

    public Shipment execute(User user, Shipment shipment) {
        authorizeLogisticsOperatorOperationService.execute(user);
        if (shipment == null || shipment.getOrder() == null) {
            throw new DomainException("The shipment must reference an order.");
        }
        Order order = shipment.getOrder();
        List<Shipment> shipments = shipmentRepositoryPort.findAllByOrder(order);
        if (shipments == null || shipments.isEmpty()) {
            throw new EntityNotFoundException("Shipment");
        }
        Shipment target = null;
        for (Shipment candidate : shipments) {
            if (ShipmentStatus.IN_PROGRESS.equals(candidate.getStatus())) {
                target = candidate;
                break;
            }
        }
        if (target == null) {
            throw new EntityNotFoundException("Shipment in progress");
        }
        target.setStatus(ShipmentStatus.COMPLETED);
        Shipment completed = shipmentRepositoryPort.update(target);
        if (allShipmentsCompleted(order)) {
            markOrderDeliveredService.execute(user, order);
        }
        return completed;
    }

    private boolean allShipmentsCompleted(Order order) {
        List<Shipment> shipments = shipmentRepositoryPort.findAllByOrder(order);
        if (shipments == null || shipments.isEmpty()) {
            return false;
        }
        for (Shipment shipment : shipments) {
            if (!ShipmentStatus.COMPLETED.equals(shipment.getStatus())) {
                return false;
            }
        }
        return true;
    }
}
