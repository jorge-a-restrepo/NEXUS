package application.domain.services.authorization;

import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes consolidated, read-only consultations of operational information.
 *
 * OBJ-12 assigns administrative consolidation to the ADMINISTRATOR, and Section
 * 5 defines the SUPERVISOR as a consultation and follow-up profile. Both roles
 * may therefore read consolidated information; neither may modify it through
 * these consultations.
 */
@Service
public class AuthorizeAdministrativeConsultationService {

    private final ValidateUserStatusService validateUserStatusService;

    public AuthorizeAdministrativeConsultationService(ValidateUserStatusService validateUserStatusService) {
        this.validateUserStatusService = validateUserStatusService;
    }

    public void execute(User user) {
        validateUserStatusService.execute(user);
        if (user.getRole() == null) {
            throw new UnauthorizedOperationException("User role is not defined.");
        }
        boolean allowed = UserRole.ADMINISTRATOR.equals(user.getRole())
                || UserRole.SUPERVISOR.equals(user.getRole());
        if (!allowed) {
            throw new UnauthorizedOperationException(
                    "Role " + user.getRole() + " is not allowed to consult consolidated information.");
        }
    }
}
