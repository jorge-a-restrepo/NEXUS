package application.domain.services.authorization;

import application.domain.User;
import application.domain.UserStatus;
import application.domain.exceptions.InvalidUserStatusException;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Validates that an operation is being executed by an authenticated user in an
 * operative status.
 *
 * RG-01: every operation must be executed by an authenticated user.
 * Domain 1: a BLOCKED user cannot operate.
 */
@Service
public class ValidateUserStatusService {

    public void execute(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("An authenticated user is required.");
        }
        if (user.getStatus() == null) {
            throw new InvalidUserStatusException("User status is not defined.");
        }
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new InvalidUserStatusException(
                    "User status " + user.getStatus() + " does not allow operations.");
        }
    }
}
