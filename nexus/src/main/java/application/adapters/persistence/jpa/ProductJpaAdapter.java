package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.Product;
import application.domain.ProductStatus;
import application.domain.ProductType;
import application.domain.Seller;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Output persistence adapter for the catalog (MySQL, tables products and product_variants).
 */
@Repository
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final SpringDataJpaProductRepository repository;

    public ProductJpaAdapter(SpringDataJpaProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        return ProductJpaMapper.toDomain(repository.save(ProductJpaMapper.toEntity(product)));
    }

    @Override
    public Product update(Product product) {
        return save(product);
    }

    @Override
    public Optional<Product> findByIdentifier(Product product) {
        if (product == null || product.getIdentifier() == null) {
            return Optional.empty();
        }
        return repository.findById(product.getIdentifier()).map(ProductJpaMapper::toDomain);
    }

    @Override
    public boolean existsByIdentifier(Product product) {
        return product != null && product.getIdentifier() != null && repository.existsById(product.getIdentifier());
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll().stream().map(ProductJpaMapper::toDomain).toList();
    }

    @Override
    public List<Product> findAllByStatus(ProductStatus productStatus) {
        if (productStatus == null) {
            return List.of();
        }
        return repository.findAllByStatus(productStatus.name()).stream().map(ProductJpaMapper::toDomain).toList();
    }

    @Override
    public List<Product> findAllByType(ProductType productType) {
        if (productType == null) {
            return List.of();
        }
        return repository.findAllByProductType(productType.name()).stream().map(ProductJpaMapper::toDomain).toList();
    }

    @Override
    public List<Product> findAllBySeller(Seller seller) {
        if (seller == null || seller.getIdentifier() == null) {
            return List.of();
        }
        return repository.findAllBySellerIdentifier(seller.getIdentifier()).stream()
                .map(ProductJpaMapper::toDomain).toList();
    }
}
