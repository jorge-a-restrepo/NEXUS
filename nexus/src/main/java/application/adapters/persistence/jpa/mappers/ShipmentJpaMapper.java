package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import application.domain.Order;
import application.domain.Shipment;
import application.domain.ShipmentStatus;
import application.domain.Warehouse;

/**
 * Bidirectional mapper between Shipment and ShipmentJpaEntity. Order and
 * warehouse are resolved by the adapter.
 */
public final class ShipmentJpaMapper {

    private ShipmentJpaMapper() {
    }

    public static ShipmentJpaEntity toEntity(Shipment domain) {
        if (domain == null) {
            return null;
        }
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setOrderIdentifier(DomainReferences.identifierOf(domain.getOrder()));
        entity.setWarehouseIdentifier(DomainReferences.identifierOf(domain.getWarehouse()));
        entity.setLogisticsOperatorIdentifier(DomainReferences.identifierOf(domain.getLogisticsOperator()));
        entity.setStatus(DomainReferences.name(domain.getStatus()));
        return entity;
    }

    public static Shipment toDomain(ShipmentJpaEntity entity, Order order, Warehouse warehouse) {
        if (entity == null) {
            return null;
        }
        Shipment domain = new Shipment();
        domain.setIdentifier(entity.getIdentifier());
        domain.setOrder(order);
        domain.setWarehouse(warehouse);
        domain.setLogisticsOperator(DomainReferences.logisticsOperator(entity.getLogisticsOperatorIdentifier()));
        domain.setStatus(DomainReferences.toEnum(ShipmentStatus.class, entity.getStatus()));
        return domain;
    }
}
