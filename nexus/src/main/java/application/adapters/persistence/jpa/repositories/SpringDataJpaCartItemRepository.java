package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.CartItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaCartItemRepository extends JpaRepository<CartItemJpaEntity, Long> {

    Optional<CartItemJpaEntity> findByCartIdentifierAndProductIdentifier(String cartIdentifier,
                                                                         String productIdentifier);

    List<CartItemJpaEntity> findAllByCartIdentifier(String cartIdentifier);
}
