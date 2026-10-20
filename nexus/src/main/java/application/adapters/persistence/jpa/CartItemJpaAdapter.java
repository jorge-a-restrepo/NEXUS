package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.CartItemJpaEntity;
import application.adapters.persistence.jpa.mappers.CartItemJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCartItemRepository;
import application.domain.Cart;
import application.domain.CartItem;
import application.domain.Product;
import application.domain.ports.out.CartItemRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for cart lines (MySQL, table cart_items).
 * Lines are located by the pair (cart, product).
 */
@Repository
public class CartItemJpaAdapter implements CartItemRepositoryPort {

    private final SpringDataJpaCartItemRepository repository;
    private final JpaReferenceResolver resolver;

    public CartItemJpaAdapter(SpringDataJpaCartItemRepository repository, JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return upsert(cartItem);
    }

    @Override
    public CartItem update(CartItem cartItem) {
        return upsert(cartItem);
    }

    @Override
    public void delete(CartItem cartItem) {
        String cartId = DomainReferences.identifierOf(cartItem == null ? null : cartItem.getCart());
        String productId = DomainReferences.identifierOf(cartItem == null ? null : cartItem.getProduct());
        if (cartId == null || productId == null) {
            return;
        }
        repository.findByCartIdentifierAndProductIdentifier(cartId, productId).ifPresent(repository::delete);
    }

    @Override
    public Optional<CartItem> findByCartAndProduct(Cart cart, Product product) {
        String cartId = DomainReferences.identifierOf(cart);
        String productId = DomainReferences.identifierOf(product);
        if (cartId == null || productId == null) {
            return Optional.empty();
        }
        return repository.findByCartIdentifierAndProductIdentifier(cartId, productId).map(this::toDomain);
    }

    @Override
    public List<CartItem> findAllByCart(Cart cart) {
        String cartId = DomainReferences.identifierOf(cart);
        if (cartId == null) {
            return List.of();
        }
        return repository.findAllByCartIdentifier(cartId).stream().map(this::toDomain).toList();
    }

    private CartItem upsert(CartItem cartItem) {
        CartItemJpaEntity entity = CartItemJpaMapper.toEntity(cartItem);
        repository.findByCartIdentifierAndProductIdentifier(entity.getCartIdentifier(), entity.getProductIdentifier())
                .ifPresent(stored -> entity.setId(stored.getId()));
        return toDomain(repository.save(entity));
    }

    private CartItem toDomain(CartItemJpaEntity entity) {
        return CartItemJpaMapper.toDomain(entity, resolver.product(entity.getProductIdentifier()));
    }
}
