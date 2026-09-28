package application.domain.exceptions;

/**
 * Raised when a required domain entity cannot be resolved through an output port.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName) {
        super(entityName + " was not found.");
    }
}
