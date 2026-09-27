package application.domain.ports.out;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.User;

import java.util.List;

/**
 * Output port for InventoryMovement persistence and querying.
 *
 * Domain 6 defines the movement types that must be traceable: INBOUND,
 * RESERVATION, SALE_OUTBOUND, ADJUSTMENT and RETURN. Movements are historical
 * records: the contract exposes creation and querying, never update or delete.
 */
public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement inventoryMovement);

    List<InventoryMovement> findAllByInventory(Inventory inventory);

    List<InventoryMovement> findAllByType(MovementType movementType);

    List<InventoryMovement> findAllByResponsibleUser(User user);
}
