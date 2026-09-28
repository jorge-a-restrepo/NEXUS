package application.domain.services.catalog;

import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves the public catalog.
 *
 * Domain 5: only PUBLISHED products are visible. This service is reachable
 * without authentication, so it never exposes anything beyond published items.
 */
@Service
public class ConsultCatalogService {

    private final ProductRepositoryPort productRepositoryPort;

    public ConsultCatalogService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    public List<Product> execute() {
        return productRepositoryPort.findAllByStatus(ProductStatus.PUBLISHED);
    }
}
