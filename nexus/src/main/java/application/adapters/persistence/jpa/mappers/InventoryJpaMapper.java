package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.domain.Inventory;
import application.domain.Product;
import application.domain.StockStatus;
import application.domain.Warehouse;

/**
 * Bidirectional mapper between Inventory and InventoryJpaEntity. The related
 * product and warehouse are resolved by the adapter.
 */
public final class InventoryJpaMapper {

    private InventoryJpaMapper() {
    }

    public static InventoryJpaEntity toEntity(Inventory domain) {
        if (domain == null) {
            return null;
        }
        InventoryJpaEntity entity = new InventoryJpaEntity();
        entity.setProductIdentifier(DomainReferences.identifierOf(domain.getProduct()));
        entity.setWarehouseIdentifier(DomainReferences.identifierOf(domain.getWarehouse()));
        entity.setAvailableQuantity(domain.getAvailableQuantity() == null ? 0 : domain.getAvailableQuantity());
        entity.setStockStatus(DomainReferences.name(domain.getStockStatus()));
        return entity;
    }

    public static Inventory toDomain(InventoryJpaEntity entity, Product product, Warehouse warehouse) {
        if (entity == null) {
            return null;
        }
        Inventory domain = new Inventory();
        domain.setProduct(product);
        domain.setWarehouse(warehouse);
        domain.setAvailableQuantity(entity.getAvailableQuantity());
        domain.setStockStatus(DomainReferences.toEnum(StockStatus.class, entity.getStockStatus()));
        return domain;
    }
}
