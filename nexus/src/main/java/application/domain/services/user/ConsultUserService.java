package application.domain.services.user;

import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Retrieves user information for the administration of the platform.
 *
 * Domain 1 and the responsibility matrix: user administration belongs to the
 * ADMINISTRATOR, so no other role reaches this service.
 */
@Service
public class ConsultUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public ConsultUserService(UserRepositoryPort userRepositoryPort,
                              AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.userRepositoryPort = userRepositoryPort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public List<User> executeAll(User user) {
        authorizeAdministratorOperationService.execute(user);
        return userRepositoryPort.findAll();
    }

    public List<User> executeByRole(User user, UserRole role) {
        authorizeAdministratorOperationService.execute(user);
        return userRepositoryPort.findAllByRole(role);
    }

    public User execute(User user, User targetUser) {
        authorizeAdministratorOperationService.execute(user);
        Optional<User> storedOptional = userRepositoryPort.findByIdentifier(targetUser);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        return storedOptional.get();
    }
}
