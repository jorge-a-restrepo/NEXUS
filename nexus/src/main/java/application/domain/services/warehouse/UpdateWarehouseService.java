package application.domain.services.warehouse;

import application.domain.User;
import application.domain.Warehouse;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Updates the information of an existing warehouse.
 *
 * The identifier is the identity of the warehouse and is never modified, since
 * inventory records are bound to it.
 */
@Service
public class UpdateWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public UpdateWarehouseService(WarehouseRepositoryPort warehouseRepositoryPort,
                                  AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public Warehouse execute(User user, Warehouse warehouse) {
        authorizeAdministratorOperationService.execute(user);
        if (warehouse == null) {
            throw new DomainException("Warehouse information must be provided.");
        }
        Optional<Warehouse> storedOptional = warehouseRepositoryPort.findByIdentifier(warehouse);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Warehouse");
        }
        Warehouse stored = storedOptional.get();
        if (warehouse.getWarehouseType() != null) {
            stored.setWarehouseType(warehouse.getWarehouseType());
        }
        return warehouseRepositoryPort.update(stored);
    }
}
