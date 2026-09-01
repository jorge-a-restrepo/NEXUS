package application.domain;

/**
 * Type of InventoryMovement. Matches Domain 6, listed explicitly in the
 * source document: Ingreso, Reserva, SalidaPorVenta, Ajuste, Devolucion.
 */
public enum MovementType {
    INBOUND,
    RESERVATION,
    SALE_OUTBOUND,
    ADJUSTMENT,
    RETURN
}
