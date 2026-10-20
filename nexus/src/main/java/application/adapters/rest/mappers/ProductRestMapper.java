package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.domain.Product;

import java.util.ArrayList;

/**
 * Converts between the product REST DTOs and the Product domain model.
 */
public final class ProductRestMapper {

    private ProductRestMapper() {
    }

    /** Builds a reference holding only the identifier, used for lookups. */
    public static Product toReference(String identifier) {
        Product product = new Product();
        product.setIdentifier(identifier);
        return product;
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setIdentifier(product.getIdentifier());
        dto.setName(product.getName());
        dto.setSellerIdentifier(product.getSeller() != null ? product.getSeller().getIdentifier() : null);
        dto.setProductType(product.getProductType() != null ? product.getProductType().name() : null);
        dto.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
        dto.setBasePrice(product.getBasePrice());
        dto.setVariants(product.getVariants() != null ? new ArrayList<>(product.getVariants()) : new ArrayList<>());
        return dto;
    }
}
