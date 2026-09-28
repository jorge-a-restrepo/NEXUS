package application.adapters.useCases;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.Order;
import application.domain.Shipment;
import application.domain.User;
import application.domain.Warehouse;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.services.inventory.AdjustInventoryService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.inventory.RegisterInventoryInflowService;
import application.domain.services.inventory.RegisterInventoryReturnService;
import application.domain.services.order.ConsultAllOrdersService;
import application.domain.services.shipment.CompleteShipmentService;
import application.domain.services.shipment.ConsultShipmentService;
import application.domain.services.shipment.CreateShipmentService;
import application.domain.services.shipment.DispatchShipmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the logistics operator input port by delegating to the domain
 * services.
 */
@Service
public class LogisticsOperatorUseCaseImpl implements LogisticsOperatorPort {

    private final ConsultInventoryService consultInventoryService;
    private final RegisterInventoryInflowService registerInventoryInflowService;
    private final AdjustInventoryService adjustInventoryService;
    private final RegisterInventoryReturnService registerInventoryReturnService;
    private final ConsultAllOrdersService consultAllOrdersService;
    private final CreateShipmentService createShipmentService;
    private final DispatchShipmentService dispatchShipmentService;
    private final CompleteShipmentService completeShipmentService;
    private final ConsultShipmentService consultShipmentService;

    public LogisticsOperatorUseCaseImpl(ConsultInventoryService consultInventoryService,
                                        RegisterInventoryInflowService registerInventoryInflowService,
                                        AdjustInventoryService adjustInventoryService,
                                        RegisterInventoryReturnService registerInventoryReturnService,
                                        ConsultAllOrdersService consultAllOrdersService,
                                        CreateShipmentService createShipmentService,
                                        DispatchShipmentService dispatchShipmentService,
                                        CompleteShipmentService completeShipmentService,
                                        ConsultShipmentService consultShipmentService) {
        this.consultInventoryService = consultInventoryService;
        this.registerInventoryInflowService = registerInventoryInflowService;
        this.adjustInventoryService = adjustInventoryService;
        this.registerInventoryReturnService = registerInventoryReturnService;
        this.consultAllOrdersService = consultAllOrdersService;
        this.createShipmentService = createShipmentService;
        this.dispatchShipmentService = dispatchShipmentService;
        this.completeShipmentService = completeShipmentService;
        this.consultShipmentService = consultShipmentService;
    }

    @Override
    public List<Inventory> consultWarehouseInventory(User user, Warehouse warehouse) {
        return consultInventoryService.executeByWarehouse(user, warehouse);
    }

    @Override
    public Inventory registerInventoryInflow(User user, InventoryMovement inventoryMovement) {
        return registerInventoryInflowService.execute(user, inventoryMovement);
    }

    @Override
    public Inventory adjustInventory(User user, InventoryMovement inventoryMovement) {
        return adjustInventoryService.execute(user, inventoryMovement);
    }

    @Override
    public Inventory registerReturnToInventory(User user, InventoryMovement inventoryMovement) {
        return registerInventoryReturnService.execute(user, inventoryMovement);
    }

    @Override
    public List<Order> consultOrdersToDispatch(User user) {
        return consultAllOrdersService.executeOrdersToDispatch(user);
    }

    @Override
    public Shipment createShipment(User user, Shipment shipment) {
        return createShipmentService.execute(user, shipment);
    }

    @Override
    public Shipment dispatchShipment(User user, Shipment shipment) {
        return dispatchShipmentService.execute(user, shipment);
    }

    @Override
    public Shipment completeShipment(User user, Shipment shipment) {
        return completeShipmentService.execute(user, shipment);
    }

    @Override
    public List<Shipment> consultShipments(User user) {
        return consultShipmentService.executeInProgress(user);
    }
}
