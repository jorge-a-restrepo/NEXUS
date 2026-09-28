package application.domain.exceptions;

/**
 * Raised when authentication fails.
 *
 * The message never states whether the email or the password was wrong, so the
 * existence of an account is not disclosed.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid credentials.");
    }
}
