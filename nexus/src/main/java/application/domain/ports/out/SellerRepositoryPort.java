package application.domain.ports.out;

import application.domain.Seller;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Seller persistence and querying.
 *
 * Domain 3 business rule: sellers cannot self-register; they are onboarded by
 * an Administrator together with their first warehouse.
 */
public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Seller update(Seller seller);

    Optional<Seller> findByIdentifier(Seller seller);

    Optional<Seller> findByEmail(Seller seller);

    boolean existsByEmail(Seller seller);

    boolean existsByIdentityDocument(Seller seller);

    List<Seller> findAll();
}
