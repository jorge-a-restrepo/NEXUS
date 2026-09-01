package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a specific product, together with its quantity, within the
 * content of a Cart. Association class required for a cart to hold
 * multiple products.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Cart cart;
    private Product product;
    private Integer quantity;
}
