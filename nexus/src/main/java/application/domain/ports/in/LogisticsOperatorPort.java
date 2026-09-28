package application.domain.ports.in;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.Order;
import application.domain.Shipment;
import application.domain.User;
import application.domain.Warehouse;

import java.util.List;

/**
 * Input port for the LOGISTICS_OPERATOR role.
 *
 * Responsibility matrix: the logistics operator manages inventory and order
 * dispatch. Shipments apply to physical products only (Domain 5); because
 * inventory is distributed (Domain 6), a single order may require several
 * shipments.
 */
public interface LogisticsOperatorPort {

    // Inventory operations

    List<Inventory> consultWarehouseInventory(User user, Warehouse warehouse);

    Inventory registerInventoryInflow(User user, InventoryMovement inventoryMovement);

    Inventory adjustInventory(User user, InventoryMovement inventoryMovement);

    Inventory registerReturnToInventory(User user, InventoryMovement inventoryMovement);

    // Dispatch operations

    List<Order> consultOrdersToDispatch(User user);

    Shipment createShipment(User user, Shipment shipment);

    Shipment dispatchShipment(User user, Shipment shipment);

    Shipment completeShipment(User user, Shipment shipment);

    List<Shipment> consultShipments(User user);
}
