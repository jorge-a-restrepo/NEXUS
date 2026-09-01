package application.domain;

/**
 * Lifecycle status of an Order. Matches Domain 7, explicit in the source
 * document: Pendiente de Pago, Pagado, Despachado, Entregado/Finalizado.
 * An order in DELIVERED state cannot be modified under any circumstance.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED
}
