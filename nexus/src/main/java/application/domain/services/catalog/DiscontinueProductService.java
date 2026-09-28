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
 * Permanently withdraws a product from the catalog.
 *
 * DISCONTINUED is a terminal status: no further transition is allowed.
 */
@Service
public class DiscontinueProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public DiscontinueProductService(ProductRepositoryPort productRepositoryPort,
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
        if (ProductStatus.DISCONTINUED.equals(stored.getStatus())) {
            throw new DomainException("The product is already discontinued.");
        }
        stored.setStatus(ProductStatus.DISCONTINUED);
        return productRepositoryPort.update(stored);
    }
}
