package application.domain;

/**
 * Classification of a Product. Matches Domain 5: "Tipo de Producto" attribute.
 * Physical products require inventory and shipment; digital products are
 * delivered immediately after payment.
 */
public enum ProductType {
    PHYSICAL,
    DIGITAL
}
