package application.adapters.persistence.mongodb.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.mongodb.documents.InventoryMovementDocument;
import application.domain.Inventory;
import application.domain.InventoryMovement;
import application.domain.MovementType;
import application.domain.UserRole;

/**
 * Bidirectional mapper between InventoryMovement and its MongoDB document.
 */
public final class InventoryMovementMongoMapper {

    private InventoryMovementMongoMapper() {
    }

    public static InventoryMovementDocument toDocument(InventoryMovement domain) {
        if (domain == null) {
            return null;
        }
        InventoryMovementDocument document = new InventoryMovementDocument();
        document.setMovementType(DomainReferences.name(domain.getMovementType()));
        document.setQuantity(domain.getQuantity());
        document.setDate(domain.getDate());
        Inventory inventory = domain.getInventory();
        if (inventory != null) {
            document.setProductIdentifier(DomainReferences.identifierOf(inventory.getProduct()));
            document.setWarehouseIdentifier(DomainReferences.identifierOf(inventory.getWarehouse()));
        }
        if (domain.getResponsibleUser() != null) {
            document.setResponsibleUserIdentifier(domain.getResponsibleUser().getIdentifier());
            document.setResponsibleUserRole(DomainReferences.name(domain.getResponsibleUser().getRole()));
        }
        return document;
    }

    public static InventoryMovement toDomain(InventoryMovementDocument document) {
        if (document == null) {
            return null;
        }
        Inventory inventory = new Inventory();
        inventory.setProduct(DomainReferences.product(document.getProductIdentifier()));
        inventory.setWarehouse(DomainReferences.warehouse(document.getWarehouseIdentifier()));

        InventoryMovement domain = new InventoryMovement();
        domain.setInventory(inventory);
        domain.setMovementType(DomainReferences.toEnum(MovementType.class, document.getMovementType()));
        domain.setQuantity(document.getQuantity());
        domain.setDate(document.getDate());
        domain.setResponsibleUser(DomainReferences.user(document.getResponsibleUserIdentifier(),
                DomainReferences.toEnum(UserRole.class, document.getResponsibleUserRole())));
        return domain;
    }
}
