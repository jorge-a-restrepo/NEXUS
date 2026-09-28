package application.domain.services.catalog;

import application.domain.Product;
import application.domain.Seller;
import application.domain.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeSellerOperationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves the products owned by the authenticated seller, in any status.
 *
 * RG-03: a seller only sees its own products.
 *
 * SUPUESTO: the restriction depends on Product referencing its Seller. Until
 * that association exists in the domain model, the output port cannot filter
 * and the isolation required by RG-03 is not effectively enforced.
 */
@Service
public class ConsultSellerProductsService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeSellerOperationService authorizeSellerOperationService;

    public ConsultSellerProductsService(ProductRepositoryPort productRepositoryPort,
                                        AuthorizeSellerOperationService authorizeSellerOperationService) {
        this.productRepositoryPort = productRepositoryPort;
        this.authorizeSellerOperationService = authorizeSellerOperationService;
    }

    public List<Product> execute(User user) {
        Seller seller = authorizeSellerOperationService.execute(user);
        return productRepositoryPort.findAllBySeller(seller);
    }
}
