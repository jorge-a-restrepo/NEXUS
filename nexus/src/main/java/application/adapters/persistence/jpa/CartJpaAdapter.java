package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.mappers.CartJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCartRepository;
import application.domain.Buyer;
import application.domain.Cart;
import application.domain.CartStatus;
import application.domain.ports.out.CartRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for carts (MySQL, table carts). The active cart is
 * always located by buyer and status, never by an identifier sent by a client.
 */
@Repository
public class CartJpaAdapter implements CartRepositoryPort {

    private final SpringDataJpaCartRepository repository;

    public CartJpaAdapter(SpringDataJpaCartRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cart save(Cart cart) {
        if (cart.getIdentifier() == null) {
            cart.setIdentifier(DomainReferences.newIdentifier());
        }
        return CartJpaMapper.toDomain(repository.save(CartJpaMapper.toEntity(cart)));
    }

    @Override
    public Cart update(Cart cart) {
        return save(cart);
    }

    @Override
    public Optional<Cart> findActiveByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findFirstByBuyerIdentifierAndStatus(buyer.getIdentifier(), CartStatus.ACTIVE.name())
                .map(CartJpaMapper::toDomain);
    }

    @Override
    public List<Cart> findAllByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentifier() == null) {
            return List.of();
        }
        return repository.findAllByBuyerIdentifier(buyer.getIdentifier()).stream()
                .map(CartJpaMapper::toDomain).toList();
    }
}
