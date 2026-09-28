package application.domain.services.catalog;

import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.Seller;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Removes a product from the public catalog without discontinuing it.
 */
@Service
public class SuspendProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public SuspendProductService(ProductRepositoryPort productRepositoryPort,
                                 AuthorizeSellerOperationService authorizeSellerOperationService,
                                 ValidateSellerOwnershipService validateSellerOwnershipService) {
        this.productRepositoryPort = productRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
        this.validateSellerOwnershipService = validateSellerOwnershipService;
    }

    public Product execute(User user, Product product) {
        Seller seller = authorizeSellerOperationService.execute(user);
        Optional<Product> storedOptional = productRepositoryPort.findByIdentifier(product);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Product");
        }
        Product stored = storedOptional.get();
        validateSellerOwnershipService.execute(seller, stored);
        if (!ProductStatus.PUBLISHED.equals(stored.getStatus())) {
            throw new DomainException("Only a published product can be suspended.");
        }
        stored.setStatus(ProductStatus.SUSPENDED);
        return productRepositoryPort.update(stored);
    }
}
