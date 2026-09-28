package application.domain;

/**
 * Commercial condition of a buyer.
 *
 * SUPUESTO: Domain 2 declares the attribute as "condición del comprador para
 * realizar compras" but does not publish its allowed values. The condition is
 * binary by definition — the buyer either may purchase or may not — so exactly
 * two values are declared and nothing beyond that is assumed.
 */
public enum CommercialStatus {
    ENABLED,
    SUSPENDED
}
