package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product data returned by the catalog endpoints.
 */
@Data
public class ProductResponseDTO {

    private String identifier;
    private String name;
    private String sellerIdentifier;
    private String productType;
    private String status;
    private BigDecimal basePrice;
    private List<String> variants;
}
