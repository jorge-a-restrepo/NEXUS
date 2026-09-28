package application.domain;

/**
 * Operative condition of the stock held in an inventory record.
 *
 * SUPUESTO: the source document names damaged stock explicitly in its critical
 * validations ("no se puede reservar inventario inexistente o marcado como
 * 'Dañado'") and describes inventory as "existencias disponibles", but it does
 * not publish a complete catalogue of stock conditions. Only those two values
 * are declared; no further value is invented.
 */
public enum StockStatus {
    AVAILABLE,
    DAMAGED
}
