package application.domain.services.warehouse;

import application.domain.User;
import application.domain.Warehouse;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

/**
 * Registers a new warehouse.
 *
 * OBJ-04 and the responsibility matrix: warehouse administration belongs to the
 * ADMINISTRATOR. Domain 4 distinguishes marketplace warehouses from seller
 * warehouses, so the type must be explicit.
 *
 * The warehouse identifier must be unique, since inventory is bound to the
 * (product, warehouse) pair.
 */
@Service
public class RegisterWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public RegisterWarehouseService(WarehouseRepositoryPort warehouseRepositoryPort,
                                    AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public Warehouse execute(User user, Warehouse warehouse) {
        authorizeAdministratorOperationService.execute(user);
        if (warehouse == null) {
            throw new DomainException("Warehouse information must be provided.");
        }
        if (warehouse.getIdentifier() == null || warehouse.getIdentifier().isBlank()) {
            throw new DomainException("Warehouse identifier must be provided.");
        }
        if (warehouse.getWarehouseType() == null) {
            throw new DomainException("Warehouse type must be provided (MARKETPLACE or SELLER).");
        }
        if (warehouseRepositoryPort.existsByIdentifier(warehouse)) {
            throw new DomainException("A warehouse with the same identifier already exists.");
        }
        return warehouseRepositoryPort.save(warehouse);
    }
}
