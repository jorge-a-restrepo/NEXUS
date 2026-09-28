package application.domain.services.user;

import application.domain.User;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateUserStatusService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Closes the session of an authenticated user.
 *
 * The domain only confirms that the session belongs to a real and operative
 * user; invalidating the access token is the responsibility of the delivery
 * adapter, since the token technology lives outside the domain.
 */
@Service
public class LogoutService {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateUserStatusService validateUserStatusService;

    public LogoutService(UserRepositoryPort userRepositoryPort,
                         ValidateUserStatusService validateUserStatusService) {
        this.userRepositoryPort = userRepositoryPort;
        this.validateUserStatusService = validateUserStatusService;
    }

    public void execute(User user) {
        validateUserStatusService.execute(user);
        Optional<User> storedOptional = userRepositoryPort.findByIdentifier(user);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
    }
}
