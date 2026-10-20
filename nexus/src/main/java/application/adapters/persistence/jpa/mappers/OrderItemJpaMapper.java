package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import application.domain.OrderItem;
import application.domain.Product;

/**
 * Bidirectional mapper between OrderItem and OrderItemJpaEntity. The product is
 * resolved by the adapter because payment and shipment read its type.
 */
public final class OrderItemJpaMapper {

    private OrderItemJpaMapper() {
    }

    public static OrderItemJpaEntity toEntity(OrderItem domain) {
        if (domain == null) {
            return null;
        }
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setOrderIdentifier(DomainReferences.identifierOf(domain.getOrder()));
        entity.setProductIdentifier(DomainReferences.identifierOf(domain.getProduct()));
        entity.setQuantity(domain.getQuantity());
        entity.setUnitPrice(domain.getUnitPrice());
        entity.setWarehouseIdentifier(DomainReferences.identifierOf(domain.getWarehouse()));
        return entity;
    }

    public static OrderItem toDomain(OrderItemJpaEntity entity, Product product) {
        if (entity == null) {
            return null;
        }
        OrderItem domain = new OrderItem();
        domain.setOrder(DomainReferences.order(entity.getOrderIdentifier()));
        domain.setProduct(product);
        domain.setQuantity(entity.getQuantity());
        domain.setUnitPrice(entity.getUnitPrice());
        domain.setWarehouse(DomainReferences.warehouse(entity.getWarehouseIdentifier()));
        return domain;
    }
}
