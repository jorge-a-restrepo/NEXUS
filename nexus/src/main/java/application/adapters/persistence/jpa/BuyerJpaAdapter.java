package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.Buyer;
import application.domain.UserRole;
import application.domain.ports.out.BuyerRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for buyers: rows of the users table with role BUYER.
 * Uniqueness checks still run against every user (RG-05).
 */
@Repository
public class BuyerJpaAdapter implements BuyerRepositoryPort {

    private static final String ROLE = UserRole.BUYER.name();

    private final SpringDataJpaUserRepository repository;

    public BuyerJpaAdapter(SpringDataJpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Buyer save(Buyer buyer) {
        if (buyer.getIdentifier() == null) {
            buyer.setIdentifier(DomainReferences.newIdentifier());
        }
        return (Buyer) UserJpaMapper.toDomain(repository.save(UserJpaMapper.toEntity(buyer)));
    }

    @Override
    public Buyer update(Buyer buyer) {
        UserJpaEntity entity = UserJpaMapper.toEntity(buyer);
        UserJpaAdapter.keepStoredPasswordWhenMissing(repository, entity);
        return (Buyer) UserJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Buyer> findByIdentifier(Buyer buyer) {
        if (buyer == null || buyer.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findByIdentifierAndRole(buyer.getIdentifier(), ROLE)
                .map(entity -> (Buyer) UserJpaMapper.toDomain(entity));
    }

    @Override
    public Optional<Buyer> findByEmail(Buyer buyer) {
        if (buyer == null || buyer.getEmail() == null) {
            return Optional.empty();
        }
        return repository.findByEmailAndRole(buyer.getEmail(), ROLE)
                .map(entity -> (Buyer) UserJpaMapper.toDomain(entity));
    }

    @Override
    public boolean existsByEmail(Buyer buyer) {
        return buyer != null && buyer.getEmail() != null && repository.existsByEmail(buyer.getEmail());
    }

    @Override
    public boolean existsByIdentityDocument(Buyer buyer) {
        return buyer != null && buyer.getIdentityDocument() != null
                && repository.existsByIdentityDocument(buyer.getIdentityDocument());
    }

    @Override
    public List<Buyer> findAll() {
        return repository.findAllByRole(ROLE).stream()
                .map(entity -> (Buyer) UserJpaMapper.toDomain(entity))
                .toList();
    }
}
