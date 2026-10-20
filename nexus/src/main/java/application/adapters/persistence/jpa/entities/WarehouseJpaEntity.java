package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for Warehouse. The owner seller is stored by identifier
 * and is null for MARKETPLACE warehouses.
 */
@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    /** WarehouseType enum name. */
    @Column(name = "warehouse_type", length = 20)
    private String warehouseType;

    /** Warehouse.seller reference. */
    @Column(name = "seller_identifier", length = 60)
    private String sellerIdentifier;
}
