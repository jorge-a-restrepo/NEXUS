package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Records an inventory movement in the traceability history.
 *
 * Domain 6 requires every stock variation to be traceable through its movement
 * type. Movements are historical records and are never modified afterwards.
 *
 * This service is invoked by the other inventory services; it is not exposed
 * directly through any input port.
 */
@Service
public class RegisterInventoryMovementService {

    private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;

    public RegisterInventoryMovementService(InventoryMovementRepositoryPort inventoryMovementRepositoryPort) {
        this.inventoryMovementRepositoryPort = inventoryMovementRepositoryPort;
    }

    public InventoryMovement execute(Inventory inventory, MovementType movementType, Integer quantity, User responsibleUser) {
        if (inventory == null) {
            throw new DomainException("Inventory must be provided to record a movement.");
        }
        if (movementType == null) {
            throw new DomainException("Movement type must be provided.");
        }
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setDate(LocalDateTime.now());
        movement.setResponsibleUser(responsibleUser);
        return inventoryMovementRepositoryPort.save(movement);
    }
}
