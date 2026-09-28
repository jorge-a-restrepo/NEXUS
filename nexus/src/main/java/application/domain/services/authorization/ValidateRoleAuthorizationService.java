package application.domain.services.authorization;

import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Validates that a user holds the role required by the requested operation.
 *
 * RG-02: each user holds exactly one role.
 * RG-03: no participant may act outside its role.
 */
@Service
public class ValidateRoleAuthorizationService {

    public void execute(User user, UserRole requiredRole) {
        if (user == null) {
            throw new UnauthorizedOperationException("An authenticated user is required.");
        }
        if (user.getRole() == null) {
            throw new UnauthorizedOperationException("User role is not defined.");
        }
        if (!requiredRole.equals(user.getRole())) {
            throw new UnauthorizedOperationException(
                    "Role " + user.getRole() + " is not allowed to perform operations reserved for " + requiredRole + ".");
        }
    }
}
