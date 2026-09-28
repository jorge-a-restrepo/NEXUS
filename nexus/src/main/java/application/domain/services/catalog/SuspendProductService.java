package application.domain.services.catalog;

import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Removes a product from the public catalog without discontinuing it.
 */
@Service
public class SuspendProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public SuspendProductService(ProductRepositoryPort productRepositoryPort,
                                 AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.productRepositoryPort = productRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public Product execute(User user, Product product) {
        authorizeSellerOperationService.execute(user);
        Optional<Product> storedOptional = productRepositoryPort.findByIdentifier(product);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Product");
        }
        Product stored = storedOptional.get();
        if (!ProductStatus.PUBLISHED.equals(stored.getStatus())) {
            throw new DomainException("Only a published product can be suspended.");
        }
        stored.setStatus(ProductStatus.SUSPENDED);
        return productRepositoryPort.update(stored);
    }
}
