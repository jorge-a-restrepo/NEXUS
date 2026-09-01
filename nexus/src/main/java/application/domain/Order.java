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

    private Buyer buyer;
    private OrderStatus status;
}
