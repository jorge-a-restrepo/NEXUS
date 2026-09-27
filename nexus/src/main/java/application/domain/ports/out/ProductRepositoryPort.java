package application.domain.ports.out;

import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.ProductType;
import application.domain.Seller;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Product catalog persistence and querying.
 *
 * Domain 5: the public catalog exposes only products in PUBLISHED status,
 * which is why status-based querying belongs to this contract.
 *
 * SUPUESTO: findAllBySeller requires the Product domain model to reference its
 * owning Seller. Without that association a seller cannot be restricted to its
 * own products (RG-03).
 */
public interface ProductRepositoryPort {

    Product save(Product product);

    Product update(Product product);

    Optional<Product> findByIdentifier(Product product);

    boolean existsByIdentifier(Product product);

    List<Product> findAll();

    List<Product> findAllByStatus(ProductStatus productStatus);

    List<Product> findAllByType(ProductType productType);

    List<Product> findAllBySeller(Seller seller);
}
