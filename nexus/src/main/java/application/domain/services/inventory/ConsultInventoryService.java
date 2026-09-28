package application.domain.services.inventory;

import application.domain.Inventory;
import application.domain.Product;
import application.domain.User;
import application.domain.Warehouse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Retrieves inventory information for the roles allowed to administer stock.
 *
 * RG-03: consultation is restricted to the roles responsible for inventory,
 * which the responsibility matrix defines as SELLER and LOGISTICS_OPERATOR.
 */
@Service
public class ConsultInventoryService {

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final AuthorizeInventoryOperationService authorizeInventoryOperationService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public ConsultInventoryService(InventoryRepositoryPort inventoryRepositoryPort,
                                   AuthorizeInventoryOperationService authorizeInventoryOperationService,
                                   AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService) {
        this.inventoryRepositoryPort = inventoryRepositoryPort;
        this.authorizeInventoryOperationService = authorizeInventoryOperationService;
        this.authorizeAdministrativeConsultationService = authorizeAdministrativeConsultationService;
    }

    /**
     * Consolidated inventory view for the administrator and the supervisor
     * (OBJ-06, OBJ-12). It is read-only and never alters stock.
     */
    public List<Inventory> executeConsolidated(User user) {
        authorizeAdministrativeConsultationService.execute(user);
        return inventoryRepositoryPort.findAll();
    }

    public List<Inventory> executeByWarehouse(User user, Warehouse warehouse) {
        authorizeInventoryOperationService.execute(user);
        if (warehouse == null) {
            throw new EntityNotFoundException("Warehouse");
        }
        return inventoryRepositoryPort.findAllByWarehouse(warehouse);
    }

    public List<Inventory> executeByProduct(User user, Product product) {
        authorizeInventoryOperationService.execute(user);
        if (product == null) {
            throw new EntityNotFoundException("Product");
        }
        return inventoryRepositoryPort.findAllByProduct(product);
    }

    public Inventory execute(User user, Product product, Warehouse warehouse) {
        authorizeInventoryOperationService.execute(user);
        if (product == null) {
            throw new EntityNotFoundException("Product");
        }
        if (warehouse == null) {
            throw new EntityNotFoundException("Warehouse");
        }
        Optional<Inventory> storedOptional = inventoryRepositoryPort.findByProductAndWarehouse(product, warehouse);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Inventory");
        }
        return storedOptional.get();
    }
}
