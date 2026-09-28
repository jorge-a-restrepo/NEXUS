package application.domain.services.user;

import application.domain.User;
import application.domain.UserRole;
import application.domain.UserStatus;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

/**
 * Registers an internal user of the marketplace: a logistics operator or a
 * supervisor.
 *
 * Domain 1 and the responsibility matrix: user administration belongs to the
 * ADMINISTRATOR. Buyers self-register and sellers are onboarded through their
 * own service, so neither role can be created here.
 *
 * RG-02: exactly one role per user.
 */
@Service
public class RegisterInternalUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public RegisterInternalUserService(UserRepositoryPort userRepositoryPort,
                                       PasswordServicePort passwordServicePort,
                                       AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public User execute(User user, User newUser) {
        authorizeAdministratorOperationService.execute(user);
        if (newUser == null) {
            throw new DomainException("User information must be provided.");
        }
        if (newUser.getFullName() == null || newUser.getFullName().isBlank()) {
            throw new DomainException("Full name must be provided.");
        }
        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            throw new DomainException("Email must be provided.");
        }
        if (newUser.getIdentityDocument() == null || newUser.getIdentityDocument().isBlank()) {
            throw new DomainException("Identity document must be provided.");
        }
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            throw new DomainException("A password must be provided.");
        }
        UserRole role = newUser.getRole();
        if (role == null) {
            throw new DomainException("A role must be assigned to the new user.");
        }
        if (!UserRole.LOGISTICS_OPERATOR.equals(role) && !UserRole.SUPERVISOR.equals(role)
                && !UserRole.ADMINISTRATOR.equals(role)) {
            throw new DomainException("Only internal users can be registered through this operation.");
        }
        if (userRepositoryPort.existsByEmail(newUser)) {
            throw new DomainException("The email is already registered in the platform.");
        }
        if (userRepositoryPort.existsByIdentityDocument(newUser)) {
            throw new DomainException("The identity document is already registered in the platform.");
        }
        newUser.setPassword(passwordServicePort.hash(newUser.getPassword()));
        newUser.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(newUser);
    }
}
