package application.domain.ports.out;

import application.domain.User;

/**
 * Output port for access token generation and validation.
 *
 * RG-01 and RG-02: every operation is performed by an authenticated user that
 * holds exactly one role. The token carries enough information to rebuild the
 * User domain model, which is the object every input port receives.
 *
 * The domain defines the contract; the concrete token technology belongs to the
 * adapter layer.
 */
public interface TokenServicePort {

    String generateToken(User user);

    boolean validateToken(String token);

    User reconstructUser(String token);
}
