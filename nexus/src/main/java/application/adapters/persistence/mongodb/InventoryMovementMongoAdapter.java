package application.adapters.persistence.mongodb;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.mongodb.mappers.InventoryMovementMongoMapper;
import application.adapters.persistence.mongodb.repositories.InventoryMovementMongoRepository;
import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.User;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Output persistence adapter for the inventory movement history (MongoDB).
 * Implements InventoryMovementRepositoryPort keeping the domain free of any
 * MongoDB dependency.
 */
@Repository
public class InventoryMovementMongoAdapter implements InventoryMovementRepositoryPort {

    private final InventoryMovementMongoRepository repository;

    public InventoryMovementMongoAdapter(InventoryMovementMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public InventoryMovement save(InventoryMovement inventoryMovement) {
        return InventoryMovementMongoMapper.toDomain(
                repository.save(InventoryMovementMongoMapper.toDocument(inventoryMovement)));
    }

    @Override
    public List<InventoryMovement> findAllByInventory(Inventory inventory) {
        String productId = DomainReferences.identifierOf(inventory == null ? null : inventory.getProduct());
        String warehouseId = DomainReferences.identifierOf(inventory == null ? null : inventory.getWarehouse());
        if (productId == null || warehouseId == null) {
            return List.of();
        }
        return repository.findAllByProductIdentifierAndWarehouseIdentifier(productId, warehouseId).stream()
                .map(InventoryMovementMongoMapper::toDomain).toList();
    }

    @Override
    public List<InventoryMovement> findAllByType(MovementType movementType) {
        if (movementType == null) {
            return List.of();
        }
        return repository.findAllByMovementType(movementType.name()).stream()
                .map(InventoryMovementMongoMapper::toDomain).toList();
    }

    @Override
    public List<InventoryMovement> findAllByResponsibleUser(User user) {
        if (user == null || user.getIdentifier() == null) {
            return List.of();
        }
        return repository.findAllByResponsibleUserIdentifier(user.getIdentifier()).stream()
                .map(InventoryMovementMongoMapper::toDomain).toList();
    }
}
