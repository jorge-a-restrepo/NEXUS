package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.Seller;
import application.domain.UserRole;
import application.domain.ports.out.SellerRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for sellers: rows of the users table with role SELLER.
 * Uniqueness checks still run against every user (RG-05).
 */
@Repository
public class SellerJpaAdapter implements SellerRepositoryPort {

    private static final String ROLE = UserRole.SELLER.name();

    private final SpringDataJpaUserRepository repository;

    public SellerJpaAdapter(SpringDataJpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Seller save(Seller seller) {
        if (seller.getIdentifier() == null) {
            seller.setIdentifier(DomainReferences.newIdentifier());
        }
        return (Seller) UserJpaMapper.toDomain(repository.save(UserJpaMapper.toEntity(seller)));
    }

    @Override
    public Seller update(Seller seller) {
        UserJpaEntity entity = UserJpaMapper.toEntity(seller);
        UserJpaAdapter.keepStoredPasswordWhenMissing(repository, entity);
        return (Seller) UserJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Seller> findByIdentifier(Seller seller) {
        if (seller == null || seller.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findByIdentifierAndRole(seller.getIdentifier(), ROLE)
                .map(entity -> (Seller) UserJpaMapper.toDomain(entity));
    }

    @Override
    public Optional<Seller> findByEmail(Seller seller) {
        if (seller == null || seller.getEmail() == null) {
            return Optional.empty();
        }
        return repository.findByEmailAndRole(seller.getEmail(), ROLE)
                .map(entity -> (Seller) UserJpaMapper.toDomain(entity));
    }

    @Override
    public boolean existsByEmail(Seller seller) {
        return seller != null && seller.getEmail() != null && repository.existsByEmail(seller.getEmail());
    }

    @Override
    public boolean existsByIdentityDocument(Seller seller) {
        return seller != null && seller.getIdentityDocument() != null
                && repository.existsByIdentityDocument(seller.getIdentityDocument());
    }

    @Override
    public List<Seller> findAll() {
        return repository.findAllByRole(ROLE).stream()
                .map(entity -> (Seller) UserJpaMapper.toDomain(entity))
                .toList();
    }
}
