package application.adapters.useCases;

import application.domain.Inventory;
import application.domain.Order;
import application.domain.Product;
import application.domain.ReturnRefund;
import application.domain.Shipment;
import application.domain.User;
import application.domain.ports.in.SupervisorPort;
import application.domain.services.catalog.ConsultCatalogService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.order.ConsultAllOrdersService;
import application.domain.services.returnrefund.ConsultReturnService;
import application.domain.services.shipment.ConsultShipmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the supervisor input port by delegating to the domain services.
 *
 * Every operation exposed here is a consultation: the supervisor is a follow-up
 * profile and no method of this class reaches a service that modifies state.
 */
@Service
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final ConsultAllOrdersService consultAllOrdersService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultShipmentService consultShipmentService;
    private final ConsultReturnService consultReturnService;
    private final ConsultCatalogService consultCatalogService;

    public SupervisorUseCaseImpl(ConsultAllOrdersService consultAllOrdersService,
                                 ConsultInventoryService consultInventoryService,
                                 ConsultShipmentService consultShipmentService,
                                 ConsultReturnService consultReturnService,
                                 ConsultCatalogService consultCatalogService) {
        this.consultAllOrdersService = consultAllOrdersService;
        this.consultInventoryService = consultInventoryService;
        this.consultShipmentService = consultShipmentService;
        this.consultReturnService = consultReturnService;
        this.consultCatalogService = consultCatalogService;
    }

    @Override
    public List<Order> consultOrders(User user) {
        return consultAllOrdersService.execute(user);
    }

    @Override
    public Order consultOrder(User user, Order order) {
        return consultAllOrdersService.executeByIdentifier(user, order);
    }

    @Override
    public List<Inventory> consultInventory(User user) {
        return consultInventoryService.executeConsolidated(user);
    }

    @Override
    public List<Shipment> consultShipments(User user) {
        return consultShipmentService.executeAll(user);
    }

    @Override
    public List<ReturnRefund> consultReturns(User user) {
        return consultReturnService.executeAll(user);
    }

    @Override
    public List<Product> consultCatalog(User user) {
        return consultCatalogService.execute();
    }
}
