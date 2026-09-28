package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.AuthorizeLogisticsOperatorOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Returns previously sold stock to the inventory of a warehouse.
 *
 * Domain 6 declares RETURN as one of the traceable movement types. The physical
 * reception of returned goods belongs to the logistics operation, so this
 * service is restricted to the LOGISTICS_OPERATOR role.
 */
@Service
public class RegisterInventoryReturnService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RegisterInventoryMovementService registerInventoryMovementService;
    private final AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService;

    public RegisterInventoryReturnService(InventoryRepositoryPort inventoryRepositoryPort,
                                          RegisterInventoryMovementService registerInventoryMovementService,
                                          AuthorizeLogisticsOperatorOperationService authorizeLogisticsOperatorOperationService) {
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.registerInventoryMovementService = registerInventoryMovementService;
        this.authorizeLogisticsOperatorOperationService = authorizeLogisticsOperatorOperationService;
    }

    public Inventory execute(User user, InventoryMovement inventoryMovement) {
        authorizeLogisticsOperatorOperationService.execute(user);
        if (inventoryMovement == null || inventoryMovement.getInventory() == null) {
            throw new DomainException("Inventory movement information must be provided.");
        }
        Integer quantity = inventoryMovement.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Returned quantity must be greater than zero.");
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
        stored.setAvailableQuantity(current + quantity);
        Inventory updated = inventoryRepositoryPort.update(stored);
        registerInventoryMovementService.execute(updated, MovementType.RETURN, quantity, user);
        return updated;
    }
}
