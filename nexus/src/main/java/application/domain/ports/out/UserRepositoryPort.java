package application.domain.ports.out;

import application.domain.User;
import application.domain.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Output port for User persistence and querying.
 *
 * Domain rule (Domain 1 / Critical Validations): identity document and email
 * must be unique across the whole platform. The existence checks declared here
 * are the contract used by the domain services to enforce that rule.
 */
public interface UserRepositoryPort {

    User save(User user);

    User update(User user);

    Optional<User> findByIdentifier(User user);

    Optional<User> findByEmail(User user);

    Optional<User> findByIdentityDocument(User user);

    boolean existsByEmail(User user);

    boolean existsByIdentityDocument(User user);

    List<User> findAll();

    List<User> findAllByRole(UserRole role);
}
