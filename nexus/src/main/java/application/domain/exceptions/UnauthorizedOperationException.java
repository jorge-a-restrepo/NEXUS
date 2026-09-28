package application.domain.exceptions;

/**
 * Raised when a user attempts an operation that does not correspond to its role.
 *
 * RG-03: no participant may manage information outside its role.
 */
public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
