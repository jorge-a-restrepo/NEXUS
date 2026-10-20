package application.adapters.rest.controllers;

import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.mappers.ProductRestMapper;
import application.domain.Product;
import application.domain.ports.in.PublicAccessPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public catalog endpoints: only PUBLISHED products are visible.
 */
@RestController
@RequestMapping("/api/v1/public/products")
public class PublicCatalogRestController {

    private final PublicAccessPort publicAccessPort;

    public PublicCatalogRestController(PublicAccessPort publicAccessPort) {
        this.publicAccessPort = publicAccessPort;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> consultCatalog() {
        List<Product> products = publicAccessPort.consultPublicCatalog();
        return ResponseEntity.ok(products.stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList());
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ProductResponseDTO> consultProduct(@PathVariable String identifier) {
        Product product = publicAccessPort.consultPublicProduct(ProductRestMapper.toReference(identifier));
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(product));
    }
}
