package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the available stock of a product in a specific warehouse.
 * Domain 6 explicitly states that inventory must be linked to a product
 * and a warehouse - this class references Product directly, not a
 * variant sub-entity. Each Inventory record corresponds to exactly one
 * Product and one Warehouse; the (Product, Warehouse) pair must not
 * generate duplicate records. Available quantity can never be negative.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Product product;
    private Warehouse warehouse;
    private Integer availableQuantity;
    private String stockStatus;
}
