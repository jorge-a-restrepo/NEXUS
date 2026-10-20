package application.domain.services.user;

import application.domain.AuthenticationResult;
import application.domain.User;
import application.domain.UserStatus;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidUserStatusException;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.TokenServicePort;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authenticates a user against its stored credentials and issues its access token.
 *
 * RG-01: every operation must be executed by an authenticated user, so this is
 * the entry point of every other flow. A BLOCKED user is rejected here and not
 * only at authorization time (Domain 1).
 *
 * A failed lookup and a wrong password produce the same exception, so the
 * service never reveals whether an account exists.
 *
 * The token is requested through TokenServicePort: the domain decides when a
 * token is issued, while the token technology stays in the adapter layer.
 */
@Service
public class LoginService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final TokenServicePort tokenServicePort;

    public LoginService(UserRepositoryPort userRepositoryPort,
                        PasswordServicePort passwordServicePort,
                        TokenServicePort tokenServicePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.tokenServicePort = tokenServicePort;
    }

    public AuthenticationResult execute(User user) {
        if (user == null) {
            throw new DomainException("Credentials must be provided.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new DomainException("Email must be provided.");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new DomainException("Password must be provided.");
        }
        Optional<User> storedOptional = userRepositoryPort.findByEmail(user);
        if (storedOptional.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        User stored = storedOptional.get();
        if (!passwordServicePort.matches(user.getPassword(), stored.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!UserStatus.ACTIVE.equals(stored.getStatus())) {
            throw new InvalidUserStatusException(
                    "User status " + stored.getStatus() + " does not allow authentication.");
        }
        String token = tokenServicePort.generateToken(stored);
        return new AuthenticationResult(stored, token);
    }
}
