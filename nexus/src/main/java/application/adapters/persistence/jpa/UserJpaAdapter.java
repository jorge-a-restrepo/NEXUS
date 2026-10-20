package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.User;
import application.domain.UserRole;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for users of any role (MySQL, table users).
 * Uniqueness checks run against the whole table: RG-05 applies platform-wide.
 */
@Repository
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataJpaUserRepository repository;

    public UserJpaAdapter(SpringDataJpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        if (user.getIdentifier() == null) {
            user.setIdentifier(DomainReferences.newIdentifier());
        }
        return UserJpaMapper.toDomain(repository.save(UserJpaMapper.toEntity(user)));
    }

    @Override
    public User update(User user) {
        UserJpaEntity entity = UserJpaMapper.toEntity(user);
        keepStoredPasswordWhenMissing(repository, entity);
        return UserJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<User> findByIdentifier(User user) {
        if (user == null || user.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findById(user.getIdentifier()).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(User user) {
        if (user == null || user.getEmail() == null) {
            return Optional.empty();
        }
        return repository.findByEmail(user.getEmail()).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByIdentityDocument(User user) {
        if (user == null || user.getIdentityDocument() == null) {
            return Optional.empty();
        }
        return repository.findByIdentityDocument(user.getIdentityDocument()).map(UserJpaMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(User user) {
        return user != null && user.getEmail() != null && repository.existsByEmail(user.getEmail());
    }

    @Override
    public boolean existsByIdentityDocument(User user) {
        return user != null && user.getIdentityDocument() != null
                && repository.existsByIdentityDocument(user.getIdentityDocument());
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(UserJpaMapper::toDomain).toList();
    }

    @Override
    public List<User> findAllByRole(UserRole role) {
        if (role == null) {
            return List.of();
        }
        return repository.findAllByRole(role.name()).stream().map(UserJpaMapper::toDomain).toList();
    }

    /** An update built without the password must never erase the stored hash. */
    static void keepStoredPasswordWhenMissing(SpringDataJpaUserRepository repository, UserJpaEntity entity) {
        if (entity.getPassword() == null && entity.getIdentifier() != null) {
            repository.findById(entity.getIdentifier())
                    .ifPresent(stored -> entity.setPassword(stored.getPassword()));
        }
    }
}
