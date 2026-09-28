package application.domain.services.authorization;

import application.domain.LogisticsOperator;
import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes an operation reserved for the LOGISTICS_OPERATOR role.
 */
@Service
public class AuthorizeLogisticsOperatorOperationService {

    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public AuthorizeLogisticsOperatorOperationService(ValidateUserStatusService validateUserStatusService,
                                                      ValidateRoleAuthorizationService validateRoleAuthorizationService) {
        this.validateUserStatusService = validateUserStatusService;
        this.validateRoleAuthorizationService = validateRoleAuthorizationService;
    }

    public LogisticsOperator execute(User user) {
        validateUserStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, UserRole.LOGISTICS_OPERATOR);
        if (!(user instanceof LogisticsOperator logisticsOperator)) {
            throw new UnauthorizedOperationException("The authenticated user is not a logistics operator.");
        }
        return logisticsOperator;
    }
}
