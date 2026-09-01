package application.domain;

/**
 * Business role assigned to a User. Matches Domain 1: "Rol" attribute.
 * A user has exactly one role (RG-02).
 */
public enum UserRole {
    BUYER,
    SELLER,
    LOGISTICS_OPERATOR,
    ADMINISTRATOR,
    SUPERVISOR
}
