package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for Order. "order" is a reserved SQL word, so the
 * table is named orders.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;

    /** Order.buyer reference. */
    @Column(name = "buyer_identifier", length = 60)
    private String buyerIdentifier;

    /** OrderStatus enum name. */
    @Column(name = "status", length = 20)
    private String status;
}
