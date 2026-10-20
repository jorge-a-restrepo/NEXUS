package application.adapters.persistence.jpa;

import application.adapters.persistence.DomainReferences;
import application.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderItemJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderItemRepository;
import application.domain.Order;
import application.domain.OrderItem;
import application.domain.Product;
import application.domain.ports.out.OrderItemRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Output persistence adapter for order lines (MySQL, table order_items).
 * Lines are only inserted: a closed order is never modified (RG-08).
 */
@Repository
public class OrderItemJpaAdapter implements OrderItemRepositoryPort {

    private final SpringDataJpaOrderItemRepository repository;
    private final JpaReferenceResolver resolver;

    public OrderItemJpaAdapter(SpringDataJpaOrderItemRepository repository, JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return toDomain(repository.save(OrderItemJpaMapper.toEntity(orderItem)));
    }

    @Override
    public List<OrderItem> findAllByOrder(Order order) {
        String orderId = DomainReferences.identifierOf(order);
        if (orderId == null) {
            return List.of();
        }
        return repository.findAllByOrderIdentifier(orderId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<OrderItem> findAllByProduct(Product product) {
        String productId = DomainReferences.identifierOf(product);
        if (productId == null) {
            return List.of();
        }
        return repository.findAllByProductIdentifier(productId).stream().map(this::toDomain).toList();
    }

    private OrderItem toDomain(OrderItemJpaEntity entity) {
        return OrderItemJpaMapper.toDomain(entity, resolver.product(entity.getProductIdentifier()));
    }
}
