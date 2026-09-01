package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a specific product, together with its quantity and price,
 * within the content of a confirmed Order. Association class required
 * for an order to hold multiple products. unitPrice is stored as a fixed
 * snapshot of the price at purchase time, not a live reference to the
 * current product price.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Order order;
    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;
}
