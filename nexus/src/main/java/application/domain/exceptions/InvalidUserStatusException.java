package application.domain.exceptions;

/**
 * Raised when the authenticated user is not in an operative status.
 *
 * Domain 1: a BLOCKED user must not be able to execute operations.
 */
public class InvalidUserStatusException extends DomainException {

    public InvalidUserStatusException(String message) {
        super(message);
    }
}
