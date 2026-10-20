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
 * JPA persistence entity for CartItem. A product appears at most once per cart;
 * the numeric id is a technical database key.
 */
@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(
        name = "uk_cart_item_cart_product",
        columnNames = {"cart_identifier", "product_identifier"}))
@Getter
@Setter
@NoArgsConstructor
public class CartItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_identifier", nullable = false, length = 60)
    private String cartIdentifier;

    @Column(name = "product_identifier", nullable = false, length = 60)
    private String productIdentifier;

    @Column(name = "quantity")
    private Integer quantity;
}
