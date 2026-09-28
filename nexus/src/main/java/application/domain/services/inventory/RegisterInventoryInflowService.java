package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.Product;
import application.domain.StockStatus;
import application.domain.User;
import application.domain.Warehouse;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Registers incoming stock for a product in a specific warehouse.
 *
 * Domain 6: inventory is distributed and always bound to a (Product, Warehouse)
 * pair, which must not be duplicated. If no record exists for that pair, it is
 * created with zero stock before applying the inflow.
 */
@Service
public class RegisterInventoryInflowService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RegisterInventoryMovementService registerInventoryMovementService;
    private final AuthorizeInventoryOperationService authorizeInventoryOperationService;

    public RegisterInventoryInflowService(InventoryRepositoryPort inventoryRepositoryPort,
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
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Inflow quantity must be greater than zero.");
        }
        Product product = inventoryMovement.getInventory().getProduct();
        Warehouse warehouse = inventoryMovement.getInventory().getWarehouse();
        if (product == null || warehouse == null) {
            throw new DomainException("Inventory must reference both a product and a warehouse.");
        }
        Optional<Inventory> storedOptional = inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse);
        Inventory inventory;
        if (storedOptional.isPresent()) {
            inventory = storedOptional.get();
            inventory.setAvailableQuantity(currentQuantity(inventory) + quantity);
            inventory = inventoryRepositoryPort.update(inventory);
        } else {
            inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setWarehouse(warehouse);
            inventory.setAvailableQuantity(quantity);
            StockStatus stockStatus = inventoryMovement.getInventory().getStockStatus();
            inventory.setStockStatus(stockStatus == null ? StockStatus.AVAILABLE : stockStatus);
            inventory = inventoryRepositoryPort.save(inventory);
        }
        registerInventoryMovementService.execute(inventory, MovementType.INBOUND, quantity, user);
        return inventory;
    }

    private int currentQuantity(Inventory inventory) {
        return inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity();
    }
}
