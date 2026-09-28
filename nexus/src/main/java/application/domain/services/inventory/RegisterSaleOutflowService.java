package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.MovementType;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Registers the definitive outflow of stock once a sale is confirmed.
 *
 * The reservation already discounted the available quantity, so this movement
 * records the sale in the traceability history without discounting twice.
 *
 * Invoked by the payment confirmation flow; role authorization is performed by
 * the calling service.
 */
@Service
public class RegisterSaleOutflowService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public RegisterSaleOutflowService(InventoryRepositoryPort inventoryRepositoryPort,
                                      RegisterInventoryMovementService registerInventoryMovementService) {
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public Inventory execute(User user, Inventory inventory, Integer quantity) {
        if (inventory == null || inventory.getProduct() == null || inventory.getWarehouse() == null) {
            throw new EntityNotFoundException("Inventory");
        }
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Sale quantity must be greater than zero.");
        }
        Optional<Inventory> storedOptional =
                inventoryRepositoryPort.findByProductAndWarehouse(inventory.getProduct(), inventory.getWarehouse());
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Inventory");
        }
        Inventory stored = storedOptional.get();
        registerInventoryMovementService.execute(stored, MovementType.SALE_OUTBOUND, quantity, user);
        return stored;
    }
}
