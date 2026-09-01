package application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a physical or digital good offered by a seller in the
 * catalog. The catalog distinguishes physical products (require inventory
 * and shipment) from digital products (immediate delivery after payment).
 * Variants (color, size, model) are represented as a list attribute of
 * Product, as literally defined in the source document (Domain 5:
 * Data Type: List) - not as a separate class.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String identifier;
    private String name;
    private ProductType productType;
    private List<String> variants;
    private ProductStatus status;
    private BigDecimal basePrice;
}
