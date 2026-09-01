package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the provisional selection of products a buyer makes before
 * confirming a purchase. Modeled as a class separate from Order (design
 * decision, see Domain Model.md) because the source document treats
 * "Cart management" as an independent, manageable process (OBJ-07).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private Buyer buyer;
    private CartStatus status;
}
