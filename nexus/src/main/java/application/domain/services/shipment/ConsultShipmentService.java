package application.domain.services.shipment;

import application.domain.Order;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves shipment information for the logistics operation and for the
 * consolidated administrative views (OBJ-10, OBJ-12).
 */
@Service
public class ConsultShipmentService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public ConsultShipmentService(ShipmentRepositoryPort shipmentRepositoryPort,
                                  AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService,
                                  AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService) {
        this.shipmentRepositoryPort = shipmentRepositoryPort;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
    }

    public List<Shipment> executeInProgress(User user) {
        authorizeLogisticsOperatorOperationService.execute(user);
        return shipmentRepositoryPort.findAllByStatus(ShipmentStatus.IN_PROGRESS);
    }

    public List<Shipment> executeByOrder(User user, Order order) {
        authorizeLogisticsOperatorOperationService.execute(user);
        if (order == null) {
            throw new EntityNotFoundException("Order");
        }
        return shipmentRepositoryPort.findAllByOrder(order);
    }

    public List<Shipment> executeConsolidated(User user, ShipmentStatus shipmentStatus) {
        authorizeAdministrativeConsultationService.execute(user);
        return shipmentRepositoryPort.findAllByStatus(shipmentStatus);
    }
}
