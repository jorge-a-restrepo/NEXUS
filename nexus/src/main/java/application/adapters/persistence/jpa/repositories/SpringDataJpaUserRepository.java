package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the users table (all roles).
 */
public interface SpringDataJpaUserRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByIdentityDocument(String identityDocument);

    Optional<UserJpaEntity> findByIdentifierAndRole(String identifier, String role);

    Optional<UserJpaEntity> findByEmailAndRole(String email, String role);

    boolean existsByEmail(String email);

    boolean existsByIdentityDocument(String identityDocument);

    List<UserJpaEntity> findAllByRole(String role);
}
