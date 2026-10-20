package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaCartRepository extends JpaRepository<CartJpaEntity, String> {

    Optional<CartJpaEntity> findFirstByBuyerIdentifierAndStatus(String buyerIdentifier, String status);

    List<CartJpaEntity> findAllByBuyerIdentifier(String buyerIdentifier);
}
