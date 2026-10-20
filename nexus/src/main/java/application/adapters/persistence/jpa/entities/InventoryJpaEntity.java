package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for Inventory.
 *
 * The real key of an inventory record is the pair (product, warehouse) (RG-10),
 * enforced with a unique constraint. The numeric id is a technical key that
 * exists only in the database and never reaches the domain.
 */
@Entity
@Table(name = "inventories", uniqueConstraints = @UniqueConstraint(
        name = "uk_inventory_product_warehouse",
        columnNames = {"product_identifier", "warehouse_identifier"}))
@Getter
@Setter
@NoArgsConstructor
public class InventoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_identifier", nullable = false, length = 60)
    private String productIdentifier;

    @Column(name = "warehouse_identifier", nullable = false, length = 60)
    private String warehouseIdentifier;

    /** Never negative (RG-06); enforced by the domain services. */
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    /** StockStatus enum name. */
    @Column(name = "stock_status", length = 20)
    private String stockStatus;
}
