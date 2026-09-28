package application.domain.services.user;

import application.domain.User;
import application.domain.UserStatus;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministratorOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Changes the operative status of a user.
 *
 * Domain 1 defines the user status as its operative condition (ACTIVE,
 * BLOCKED). A BLOCKED user cannot execute any operation, which is enforced by
 * the authorization services on every request.
 */
@Service
public class ChangeUserStatusService {

    private final UserRepositoryPort userRepositoryPort;
    private final AuthorizeAdministratorOperationService authorizeAdministratorOperationService;

    public ChangeUserStatusService(UserRepositoryPort userRepositoryPort,
                                   AuthorizeAdministratorOperationService authorizeAdministratorOperationService) {
        this.userRepositoryPort = userRepositoryPort;
        this.authorizeAdministratorOperationService = authorizeAdministratorOperationService;
    }

    public User execute(User user, User targetUser, UserStatus newStatus) {
        authorizeAdministratorOperationService.execute(user);
        if (targetUser == null) {
            throw new EntityNotFoundException("User");
        }
        if (newStatus == null) {
            throw new DomainException("The new status must be provided.");
        }
        Optional<User> storedOptional = userRepositoryPort.findByIdentifier(targetUser);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        User stored = storedOptional.get();
        if (newStatus.equals(stored.getStatus())) {
            throw new DomainException("The user already has the requested status.");
        }
        stored.setStatus(newStatus);
        return userRepositoryPort.update(stored);
    }
}
