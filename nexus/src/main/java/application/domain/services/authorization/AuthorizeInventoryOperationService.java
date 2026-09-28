package application.domain.services.authorization;

import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes inventory administration operations.
 *
 * Responsibility matrix: inventory administration is shared by the SELLER and
 * the LOGISTICS_OPERATOR roles, so a single-role check is not sufficient here.
 */
@Service
public class AuthorizeInventoryOperationService {

    private final ValidateUserStatusService validateUserStatusService;

    public AuthorizeInventoryOperationService(ValidateUserStatusService validateUserStatusService) {
        this.validateUserStatusService = validateUserStatusService;
    }

    public void execute(User user) {
        validateUserStatusService.execute(user);
        if (user.getRole() == null) {
            throw new UnauthorizedOperationException("User role is not defined.");
        }
        boolean allowed = UserRole.SELLER.equals(user.getRole())
                || UserRole.LOGISTICS_OPERATOR.equals(user.getRole());
        if (!allowed) {
            throw new UnauthorizedOperationException(
                    "Role " + user.getRole() + " is not allowed to administer inventory.");
        }
    }
}
