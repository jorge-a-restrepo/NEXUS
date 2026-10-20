package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.ReturnRefundJpaEntity;
import application.domain.Order;
import application.domain.ReturnRefund;
import application.domain.ReturnStatus;

/**
 * Bidirectional mapper between ReturnRefund and ReturnRefundJpaEntity. The order
 * is resolved by the adapter because ownership checks read its buyer.
 */
public final class ReturnRefundJpaMapper {

    private ReturnRefundJpaMapper() {
    }

    public static ReturnRefundJpaEntity toEntity(ReturnRefund domain) {
        if (domain == null) {
            return null;
        }
        ReturnRefundJpaEntity entity = new ReturnRefundJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setOrderIdentifier(DomainReferences.identifierOf(domain.getOrder()));
        entity.setReason(domain.getReason());
        entity.setStatus(DomainReferences.name(domain.getStatus()));
        entity.setRefundedAmount(domain.getRefundedAmount());
        return entity;
    }

    public static ReturnRefund toDomain(ReturnRefundJpaEntity entity, Order order) {
        if (entity == null) {
            return null;
        }
        ReturnRefund domain = new ReturnRefund();
        domain.setIdentifier(entity.getIdentifier());
        domain.setOrder(order);
        domain.setReason(entity.getReason());
        domain.setStatus(DomainReferences.toEnum(ReturnStatus.class, entity.getStatus()));
        domain.setRefundedAmount(entity.getRefundedAmount());
        return domain;
    }
}
