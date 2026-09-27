package application.domain.ports.out;

/**
 * Output port for password hashing and verification.
 *
 * RG-01 requires every operation to be executed by an authenticated user, which
 * implies credential storage and verification. The domain never implements or
 * knows the hashing algorithm: it only depends on this contract.
 *
 * SUPUESTO: the User domain model does not declare a credential attribute yet.
 * Authentication requires adding it.
 */
public interface PasswordServicePort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
