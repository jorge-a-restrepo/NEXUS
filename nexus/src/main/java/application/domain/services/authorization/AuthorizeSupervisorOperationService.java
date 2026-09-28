package application.domain.services.authorization;

import application.domain.Supervisor;
import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes a consultation reserved for the SUPERVISOR role.
 *
 * Section 5 defines the supervisor as a read-only profile, so services using
 * this authorization must never modify business state.
 */
@Service
public class AuthorizeSupervisorOperationService {

    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public AuthorizeSupervisorOperationService(ValidateUserStatusService validateUserStatusService,
                                               ValidateRoleAuthorizationService validateRoleAuthorizationService) {
        this.validateUserStatusService = validateUserStatusService;
        this.validateRoleAuthorizationService = validateRoleAuthorizationService;
    }

    public Supervisor execute(User user) {
        validateUserStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, UserRole.SUPERVISOR);
        if (!(user instanceof Supervisor supervisor)) {
            throw new UnauthorizedOperationException("The authenticated user is not a supervisor.");
        }
        return supervisor;
    }
}
