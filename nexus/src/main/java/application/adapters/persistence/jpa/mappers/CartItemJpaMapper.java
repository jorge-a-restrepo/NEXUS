package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.CartItemJpaEntity;
import application.domain.CartItem;
import application.domain.Product;

/**
 * Bidirectional mapper between CartItem and CartItemJpaEntity. The product is
 * resolved by the adapter because checkout reads its type and base price.
 */
public final class CartItemJpaMapper {

    private CartItemJpaMapper() {
    }

    public static CartItemJpaEntity toEntity(CartItem domain) {
        if (domain == null) {
            return null;
        }
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.setCartIdentifier(DomainReferences.identifierOf(domain.getCart()));
        entity.setProductIdentifier(DomainReferences.identifierOf(domain.getProduct()));
        entity.setQuantity(domain.getQuantity());
        return entity;
    }

    public static CartItem toDomain(CartItemJpaEntity entity, Product product) {
        if (entity == null) {
            return null;
        }
        CartItem domain = new CartItem();
        domain.setCart(DomainReferences.cart(entity.getCartIdentifier()));
        domain.setProduct(product);
        domain.setQuantity(entity.getQuantity());
        return domain;
    }
}
