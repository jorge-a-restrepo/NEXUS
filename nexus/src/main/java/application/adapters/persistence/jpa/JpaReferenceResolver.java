package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.Order;
import application.domain.Product;
import application.domain.Warehouse;
import org.springframework.stereotype.Component;

/**
 * Loads the full related object when a domain service reads more than its
 * identity, for example orderItem.getProduct().getProductType() or
 * invoice.getOrder().getBuyer(). Falls back to an identifier-only reference
 * when the related row does not exist.
 */
@Component
public class JpaReferenceResolver {

    private final SpringDataJpaProductRepository productRepository;
    private final SpringDataJpaWarehouseRepository warehouseRepository;
    private final SpringDataJpaOrderRepository orderRepository;

    public JpaReferenceResolver(SpringDataJpaProductRepository productRepository,
                                SpringDataJpaWarehouseRepository warehouseRepository,
                                SpringDataJpaOrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.orderRepository = orderRepository;
    }

    public Product product(String identifier) {
        if (identifier == null) {
            return null;
        }
        return productRepository.findById(identifier)
                .map(ProductJpaMapper::toDomain)
                .orElseGet(() -> DomainReferences.product(identifier));
    }

    public Warehouse warehouse(String identifier) {
        if (identifier == null) {
            return null;
        }
        return warehouseRepository.findById(identifier)
                .map(WarehouseJpaMapper::toDomain)
                .orElseGet(() -> DomainReferences.warehouse(identifier));
    }

    public Order order(String identifier) {
        if (identifier == null) {
            return null;
        }
        return orderRepository.findById(identifier)
                .map(OrderJpaMapper::toDomain)
                .orElseGet(() -> DomainReferences.order(identifier));
    }
}
