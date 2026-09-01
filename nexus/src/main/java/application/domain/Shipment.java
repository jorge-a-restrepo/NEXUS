package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * INFERRED CLASS.
 * Represents the logistics process of transporting a physical order from
 * a warehouse to the buyer. The source document includes "Shipment
 * management" within scope (OBJ-10, section 3.1) but does not develop a
 * full domain with attributes. The Order 1 - 0..N Shipment relationship
 * is kept because distributed inventory across warehouses (Domain 6)
 * makes it necessary for a single order to require more than one
 * dispatch. ItemShipment (per-product-per-shipment detail) is
 * intentionally NOT modeled - the source document does not require that
 * level of granularity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    private Order order;
    private Warehouse warehouse;
    private LogisticsOperator logisticsOperator;
    private ShipmentStatus status;
}
