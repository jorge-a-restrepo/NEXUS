package application.domain.services.authorization;

import application.domain.Buyer;
import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes an operation reserved for the BUYER role and returns the
 * authenticated user as its Buyer specialization.
 */
@Service
public class AuthorizeBuyerOperationService {

    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public AuthorizeBuyerOperationService(ValidateUserStatusService validateUserStatusService,
                                          ValidateRoleAuthorizationService validateRoleAuthorizationService) {
        this.validateUserStatusService = validateUserStatusService;
        this.validateRoleAuthorizationService = validateRoleAuthorizationService;
    }

    public Buyer execute(User user) {
        validateUserStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, UserRole.BUYER);
        if (!(user instanceof Buyer buyer)) {
            throw new UnauthorizedOperationException("The authenticated user is not a buyer.");
        }
        return buyer;
    }
}
