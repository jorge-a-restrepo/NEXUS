package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the formal commercial commitment between a buyer and the
 * marketplace. Its lifecycle is explicitly defined in Domain 7. An order
 * in DELIVERED status cannot be modified under any circumstance
 * (explicit critical validation).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Unique identifier of the order. The order is the formal commercial
     * commitment of Domain 7 and is referenced by its invoice, its shipments and
     * its return requests, so it needs an identity of its own.
     */
    private String identifier;
    private Buyer buyer;
    private OrderStatus status;
}
