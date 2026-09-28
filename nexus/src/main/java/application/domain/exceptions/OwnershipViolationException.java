package application.domain.exceptions;

/**
 * Raised when a user attempts to operate over information that does not belong
 * to it.
 *
 * Domain 2: a buyer never manages information of other buyers.
 * RG-03: participants only access information within their own scope.
 */
public class OwnershipViolationException extends DomainException {

    public OwnershipViolationException(String message) {
        super(message);
    }
}
