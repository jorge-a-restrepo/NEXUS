package application.domain.services.catalog;

import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.Seller;
import application.domain.User;
import application.domain.exceptions.DomainException;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Registers a new product in the catalog on behalf of a seller.
 *
 * Responsibility matrix: product registration belongs to the SELLER role.
 *
 * SUPUESTO: the business flow separates "Catálogo" (registration) from
 * "Publicación" (becoming visible), but ProductStatus only declares PUBLISHED,
 * SUSPENDED and DISCONTINUED. A newly registered product is therefore stored as
 * SUSPENDED, meaning not yet visible, and becomes PUBLISHED through
 * PublishProductService. No new status value is invented.
 */
@Service
public class RegisterProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public RegisterProductService(ProductRepositoryPort productRepositoryPort,
                                  AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.productRepositoryPort = productRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public Product execute(User user, Product product) {
        Seller seller = authorizeSellerOperationService.execute(user);
        if (product == null) {
            throw new DomainException("Product information must be provided.");
        }
        if (product.getIdentifier() == null || product.getIdentifier().isBlank()) {
            throw new DomainException("Product identifier must be provided.");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new DomainException("Product name must be provided.");
        }
        if (product.getProductType() == null) {
            throw new DomainException("Product type must be provided (PHYSICAL or DIGITAL).");
        }
        if (product.getBasePrice() == null || product.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Product base price must be greater than zero.");
        }
        if (productRepositoryPort.existsByIdentifier(product)) {
            throw new DomainException("A product with the same identifier already exists.");
        }
        product.setSeller(seller);
        product.setStatus(ProductStatus.SUSPENDED);
        return productRepositoryPort.save(product);
    }
}
