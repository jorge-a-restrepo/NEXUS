package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.CartJpaEntity;
import application.domain.Cart;
import application.domain.CartStatus;

/**
 * Bidirectional mapper between Cart and CartJpaEntity.
 */
public final class CartJpaMapper {

    private CartJpaMapper() {
    }

    public static CartJpaEntity toEntity(Cart domain) {
        if (domain == null) {
            return null;
        }
        CartJpaEntity entity = new CartJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setBuyerIdentifier(DomainReferences.identifierOf(domain.getBuyer()));
        entity.setStatus(DomainReferences.name(domain.getStatus()));
        return entity;
    }

    public static Cart toDomain(CartJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Cart domain = new Cart();
        domain.setIdentifier(entity.getIdentifier());
        domain.setBuyer(DomainReferences.buyer(entity.getBuyerIdentifier()));
        domain.setStatus(DomainReferences.toEnum(CartStatus.class, entity.getStatus()));
        return domain;
    }
}
