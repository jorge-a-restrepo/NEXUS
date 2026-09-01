package application.domain;

/**
 * Operating status of a User account. Matches Domain 1: "Estado" attribute.
 * Only ACTIVE and BLOCKED are kept because those are the only two values
 * cited explicitly in the source document ("Activo, Bloqueado, etc.").
 * No additional value is added despite the document's "etc.", per the
 * strict fidelity criterion.
 */
public enum UserStatus {
    ACTIVE,
    BLOCKED
}
