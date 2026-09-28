package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.MovementType;
import application.domain.Product;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reserves stock of a product for an order being confirmed.
 *
 * Critical validations (Section 11):
 * - stock that does not exist cannot be reserved;
 * - stock marked as damaged cannot be reserved;
 * - available quantity can never become negative (Domain 6).
 *
 * Because inventory is distributed, the reservation walks the warehouses that
 * hold the product and takes stock from the first one able to satisfy it.
 *
 * This service is invoked by the checkout flow on behalf of a buyer, so it does
 * not perform role authorization: the calling service already authorized the
 * buyer.
 *
 * SUPUESTO: stockStatus is still an open String in the domain model, so the
 * damaged condition is compared against the literal "DAMAGED". It must become a
 * bounded value object.
 */
@Service
public class ReserveInventoryService {

    private static final String DAMAGED_STOCK = "DAMAGED";

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public ReserveInventoryService(InventoryRepositoryPort inventoryRepositoryPort,
                                   RegisterInventoryMovementService registerInventoryMovementService) {
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public Inventory execute(User user, Product product, Integer quantity) {
        if (product == null) {
            throw new EntityNotFoundException("Product");
        }
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Reserved quantity must be greater than zero.");
        }
        List<Inventory> inventories = inventoryRepositoryPort.findAllByProduct(product);
        if (inventories == null || inventories.isEmpty()) {
            throw new DomainException("There is no inventory registered for the requested product.");
        }
        Inventory selected = null;
        for (Inventory inventory : inventories) {
            if (isDamaged(inventory)) {
                continue;
            }
            if (availableQuantity(inventory) >= quantity) {
                selected = inventory;
                break;
            }
        }
        if (selected == null) {
            throw new DomainException("There is not enough available stock to reserve the requested quantity.");
        }
        selected.setAvailableQuantity(availableQuantity(selected) - quantity);
        Inventory updated = inventoryRepositoryPort.update(selected);
        registerInventoryMovementService.execute(updated, MovementType.RESERVATION, quantity, user);
        return updated;
    }

    private boolean isDamaged(Inventory inventory) {
        return inventory.getStockStatus() != null && DAMAGED_STOCK.equalsIgnoreCase(inventory.getStockStatus());
    }

    private int availableQuantity(Inventory inventory) {
        return inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity();
    }
}
