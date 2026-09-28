package application.domain.exceptions;

/**
 * Base exception for every business rule violation raised by the domain.
 *
 * The domain never throws framework or persistence exceptions: adapters
 * translate these into transport-specific responses.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
