package application.domain.services.authorization;

import application.domain.Administrator;
import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes an operation reserved for the ADMINISTRATOR role.
 *
 * Domain 3: seller onboarding is exclusive to this role.
 */
@Service
public class AuthorizeAdministratorOperationService {

    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public AuthorizeAdministratorOperationService(ValidateUserStatusService validateUserStatusService,
                                                  ValidateRoleAuthorizationService validateRoleAuthorizationService) {
        this.validateUserStatusService = validateUserStatusService;
        this.validateRoleAuthorizationService = validateRoleAuthorizationService;
    }

    public Administrator execute(User user) {
        validateUserStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, UserRole.ADMINISTRATOR);
        if (!(user instanceof Administrator administrator)) {
            throw new UnauthorizedOperationException("The authenticated user is not an administrator.");
        }
        return administrator;
    }
}
