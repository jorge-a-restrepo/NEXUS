package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Applies a manual correction to the stock of a product in a warehouse.
 *
 * The adjustment may be positive or negative, but Domain 6 forbids negative
 * stock under any circumstance, so an adjustment that would leave the inventory
 * below zero is rejected.
 */
@Service
public class AdjustInventoryService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RegisterInventoryMovementService registerInventoryMovementService;
    private final AuthorizeInventoryOperationService authorizeInventoryOperationService;

    public AdjustInventoryService(InventoryRepositoryPort inventoryRepositoryPort,
                                  RegisterInventoryMovementService registerInventoryMovementService,
                                  AuthorizeInventoryOperationService authorizeInventoryOperationService) {
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.registerInventoryMovementService = registerInventoryMovementService;
        this.authorizeInventoryOperationService = authorizeInventoryOperationService;
    }

    public Inventory execute(User user, InventoryMovement inventoryMovement) {
        authorizeInventoryOperationService.execute(user);
        if (inventoryMovement == null || inventoryMovement.getInventory() == null) {
            throw new DomainException("Inventory movement information must be provided.");
        }
        Integer quantity = inventoryMovement.getQuantity();
        if (quantity == null || quantity == 0) {
            throw new DomainException("Adjustment quantity must be different from zero.");
        }
        Inventory reference = inventoryMovement.getInventory();
        if (reference.getProduct() == null || reference.getWarehouse() == null) {
            throw new DomainException("Inventory must reference both a product and a warehouse.");
        }
        Optional<Inventory> storedOptional =
                inventoryRepositoryPort.findByProductAndWarehouse(reference.getProduct(), reference.getWarehouse());
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Inventory");
        }
        Inventory stored = storedOptional.get();
        int current = stored.getAvailableQuantity() == null ? 0 : stored.getAvailableQuantity();
        int result = current + quantity;
        if (result < 0) {
            throw new DomainException("The adjustment would leave a negative stock, which is not allowed.");
        }
        stored.setAvailableQuantity(result);
        Inventory updated = inventoryRepositoryPort.update(stored);
        registerInventoryMovementService.execute(updated, MovementType.ADJUSTMENT, quantity, user);
        return updated;
    }
}
