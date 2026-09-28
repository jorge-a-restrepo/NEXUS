package application.domain.services.shipment;

import application.domain.Order;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import application.domain.services.order.MarkOrderShippedService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registers the physical departure of a shipment from the warehouse.
 *
 * Business flow step "Logística". The order moves to SHIPPED through the order
 * service, which keeps the lifecycle rules of Domain 7 in a single place.
 */
@Service
public class DispatchShipmentService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final MarkOrderShippedService markOrderShippedService;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public DispatchShipmentService(ShipmentRepositoryPort shipmentRepositoryPort,
                                   MarkOrderShippedService markOrderShippedService,
                                   AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService) {
        this.shipmentRepositoryPort = shipmentRepositoryPort;
        this.markOrderShippedService = markOrderShippedService;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
    }

    public Shipment execute(User user, Shipment shipment) {
        authorizeLogisticsOperatorOperationService.execute(user);
        Shipment stored = resolveShipment(shipment);
        if (!ShipmentStatus.IN_PROGRESS.equals(stored.getStatus())) {
            throw new DomainException("Only a shipment in progress can be dispatched.");
        }
        markOrderShippedService.execute(user, stored.getOrder());
        return stored;
    }

    private Shipment resolveShipment(Shipment shipment) {
        if (shipment == null || shipment.getOrder() == null) {
            throw new DomainException("The shipment must reference an order.");
        }
        List<Shipment> shipments = shipmentRepositoryPort.findAllByOrder(shipment.getOrder());
        if (shipments == null || shipments.isEmpty()) {
            throw new EntityNotFoundException("Shipment");
        }
        for (Shipment candidate : shipments) {
            if (ShipmentStatus.IN_PROGRESS.equals(candidate.getStatus())) {
                return candidate;
            }
        }
        throw new EntityNotFoundException("Shipment in progress");
    }
}
