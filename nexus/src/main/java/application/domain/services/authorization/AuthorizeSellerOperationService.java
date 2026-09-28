package application.domain.services.authorization;

import application.domain.Seller;
import application.domain.User;
import application.domain.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import org.springframework.stereotype.Service;

/**
 * Authorizes an operation reserved for the SELLER role and returns the
 * authenticated user as its Seller specialization.
 */
@Service
public class AuthorizeSellerOperationService {

    private final ValidateUserStatusService validateUserStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public AuthorizeSellerOperationService(ValidateUserStatusService validateUserStatusService,
                                           ValidateRoleAuthorizationService validateRoleAuthorizationService) {
        this.validateUserStatusService = validateUserStatusService;
        this.validateRoleAuthorizationService = validateRoleAuthorizationService;
    }

    public Seller execute(User user) {
        validateUserStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, UserRole.SELLER);
        if (!(user instanceof Seller seller)) {
            throw new UnauthorizedOperationException("The authenticated user is not a seller.");
        }
        return seller;
    }
}
