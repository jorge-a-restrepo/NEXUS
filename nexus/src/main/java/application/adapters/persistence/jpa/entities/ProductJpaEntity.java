package application.adapters.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for Product. Variants are a list of values inside the
 * product (design decision of the domain model), stored in product_variants.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    @Column(name = "name", length = 150)
    private String name;

    /** Product.seller reference (RG-03). */
    @Column(name = "seller_identifier", length = 60)
    private String sellerIdentifier;

    /** ProductType enum name. */
    @Column(name = "product_type", length = 20)
    private String productType;

    /** ProductStatus enum name. */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "base_price", precision = 19, scale = 2)
    private BigDecimal basePrice;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_variants", joinColumns = @JoinColumn(name = "product_identifier"))
    @Column(name = "variant", length = 100)
    private List<String> variants = new ArrayList<>();
}
