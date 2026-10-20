package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.domain.Order;
import application.domain.OrderStatus;

/**
 * Bidirectional mapper between Order and OrderJpaEntity.
 */
public final class OrderJpaMapper {

    private OrderJpaMapper() {
    }

    public static OrderJpaEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setBuyerIdentifier(DomainReferences.identifierOf(domain.getBuyer()));
        entity.setStatus(DomainReferences.name(domain.getStatus()));
        return entity;
    }

    public static Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Order domain = new Order();
        domain.setIdentifier(entity.getIdentifier());
        domain.setBuyer(DomainReferences.buyer(entity.getBuyerIdentifier()));
        domain.setStatus(DomainReferences.toEnum(OrderStatus.class, entity.getStatus()));
        return domain;
    }
}
