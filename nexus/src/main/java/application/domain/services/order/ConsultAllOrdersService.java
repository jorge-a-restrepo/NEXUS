package application.domain.services.order;

import application.domain.Order;
import application.domain.OrderStatus;
import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves consolidated order information.
 *
 * OBJ-12: the administrator consolidates administrative information and the
 * supervisor follows up the operation. Both are read-only consultations.
 *
 * The dispatch queue is exposed separately for the logistics operator, which
 * only needs the orders already paid and waiting to leave the warehouse.
 */
@Service
public class ConsultAllOrdersService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public ConsultAllOrdersService(OrderRepositoryPort orderRepositoryPort,
                                   AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService,
                                   AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
    }

    public List<Order> execute(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return orderRepositoryPort.findAll();
    }

    public Order executeByIdentifier(User user, Order order) {
        authorizeAdministrativeConsultationService.execute(user);
        return orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
    }

    public List<Order> executeOrdersToDispatch(User user) {
        authorizeLogisticsOperatorOperationService.execute(user);
        return orderRepositoryPort.findAllByStatus(OrderStatus.PAID);
    }
}
