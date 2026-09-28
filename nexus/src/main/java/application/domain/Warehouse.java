package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a physical storage space where marketplace inventory is
 * managed. Explicitly classified as either a Marketplace warehouse or a
 * Seller warehouse (Domain 4). Every inventory record must be linked to
 * a warehouse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    private String identifier;
    private WarehouseType warehouseType;
    /**
     * Seller that owns the warehouse. Domain 4 distinguishes marketplace
     * warehouses from seller warehouses; it is null for MARKETPLACE ones.
     */
    private Seller seller;
}
