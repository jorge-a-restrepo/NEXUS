package application.domain.ports.out;

import application.domain.Buyer;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Buyer persistence and querying.
 *
 * Buyers are the only participants allowed to self-register (Section 3.1:
 * "Registro de compradores"), in contrast with Sellers, who are onboarded
 * exclusively by an Administrator (Domain 3).
 */
public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    Buyer update(Buyer buyer);

    Optional<Buyer> findByIdentifier(Buyer buyer);

    Optional<Buyer> findByEmail(Buyer buyer);

    boolean existsByEmail(Buyer buyer);

    boolean existsByIdentityDocument(Buyer buyer);

    List<Buyer> findAll();
}
