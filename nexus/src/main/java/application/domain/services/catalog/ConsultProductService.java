package application.domain.services.catalog;

import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Retrieves a single product from the public catalog.
 *
 * A product that is not PUBLISHED is not visible to the public, so it is
 * reported as not found rather than disclosing its existence.
 */
@Service
public class ConsultProductService {

    private final ProductRepositoryPort productRepositoryPort;

    public ConsultProductService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    public Product execute(Product product) {
        Optional<Product> storedOptional = productRepositoryPort.findByIdentifier(product);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Product");
        }
        Product stored = storedOptional.get();
        if (!ProductStatus.PUBLISHED.equals(stored.getStatus())) {
            throw new EntityNotFoundException("Product");
        }
        return stored;
    }
}
