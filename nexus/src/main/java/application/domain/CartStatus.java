package application.domain;

/**
 * Status of a Cart. Inferred - necessary to know whether the cart is still
 * active or was already used to generate an Order.
 */
public enum CartStatus {
    ACTIVE,
    CONVERTED,
    ABANDONED
}
