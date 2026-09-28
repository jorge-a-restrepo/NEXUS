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

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Updates the commercial information of an existing product.
 *
 * A DISCONTINUED product is a closed concept and cannot be modified.
 */
@Service
public class UpdateProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public UpdateProductService(ProductRepositoryPort productRepositoryPort,
                                AuthorizeSellerOperationService authorizeSellerOperationService,
                                 ValidateSellerOwnershipService validateSellerOwnershipService) {
        this.productRepositoryPort = productRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
        this.validateSellerOwnershipService = validateSellerOwnershipService;
    }

    public Product execute(User user, Product product) {
        Seller seller = authorizeSellerOperationService.execute(user);
        if (product == null) {
            throw new DomainException("Product information must be provided.");
        }
        Optional<Product> storedOptional = productRepositoryPort.findByIdentifier(product);
        if (storedOptional.isEmpty()) {
            throw new EntityNotFoundException("Product");
        }
        Product stored = storedOptional.get();
        validateSellerOwnershipService.execute(seller, stored);
        if (ProductStatus.DISCONTINUED.equals(stored.getStatus())) {
            throw new DomainException("A discontinued product cannot be modified.");
        }
        if (product.getName() != null && !product.getName().isBlank()) {
            stored.setName(product.getName());
        }
        if (product.getBasePrice() != null) {
            if (product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("Product base price must be greater than zero.");
            }
            stored.setBasePrice(product.getBasePrice());
        }
        if (product.getVariants() != null) {
            stored.setVariants(product.getVariants());
        }
        return productRepositoryPort.update(stored);
    }
}
